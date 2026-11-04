package com.kintsu.app.wellbeing.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.kintsu.app.MainActivity
import com.kintsu.app.data.KintsuDatabase
import com.kintsu.app.wellbeing.domain.WebsiteFilterManager
import com.kintsu.app.wellbeing.domain.AdultContentFilterEngine
import com.kintsu.app.data.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer

class KintsuVpnDnsFilterService : VpnService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false

    companion object {
        const val NOTIFICATION_ID = 8821
        const val CHANNEL_ID = "kintsu_dns_protection_channel"
        const val ACTION_START = "com.kintsu.app.START_DNS_FILTER"
        const val ACTION_STOP = "com.kintsu.app.STOP_DNS_FILTER"

        @Volatile
        var isVpnRunning: Boolean = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, KintsuVpnDnsFilterService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, KintsuVpnDnsFilterService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopVpn()
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                startVpn()
                return START_STICKY
            }
        }
    }

    private fun startVpn() {
        if (isRunning) return
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())

        try {
            val builder = Builder()
                .setSession("Kintsu Digital Balance")
                .addAddress("10.111.0.1", 32)
                .addDnsServer("1.1.1.3")
                .addDnsServer("1.0.0.3")
                .addRoute("10.111.0.1", 32)
                .setMtu(1500)

            // Disallow self traffic so our sockets don't loop
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                builder.setMetered(false)
            }
            try {
                builder.addDisallowedApplication(packageName)
            } catch (_: Exception) {}

            vpnInterface = builder.establish()
            if (vpnInterface != null) {
                isRunning = true
                isVpnRunning = true
                serviceScope.launch {
                    runPacketLoop()
                }
            } else {
                stopSelf()
            }
        } catch (e: Exception) {
            stopSelf()
        }
    }

    private suspend fun runPacketLoop() {
        val pfd = vpnInterface ?: return
        val inputStream = FileInputStream(pfd.fileDescriptor)
        val outputStream = FileOutputStream(pfd.fileDescriptor)
        val packet = ByteBuffer.allocate(32767)

        val db = KintsuDatabase.getDatabase(applicationContext)

        val upstreamSocket = DatagramSocket().apply {
            protect(this)
            soTimeout = 2500
        }
        val settingsStore = SettingsDataStore(applicationContext)
        val upstreamDns = InetAddress.getByName("1.1.1.3")

        try {
            while (isRunning) {
                packet.clear()
                val length = inputStream.read(packet.array())
                if (length > 0) {
                    packet.limit(length)
                    // Check if IPv4 and UDP
                    val buffer = packet.array()
                    val version = (buffer[0].toInt() shr 4) and 0x0F
                    val protocol = buffer[9].toInt() and 0xFF

                    if (version == 4 && protocol == 17) { // IPv4 & UDP
                        val ipHeaderLen = (buffer[0].toInt() and 0x0F) * 4
                        val destPort = ((buffer[ipHeaderLen].toInt() and 0xFF) shl 8) or
                                (buffer[ipHeaderLen + 1].toInt() and 0xFF)

                        if (destPort == 53) { // DNS Query
                            val dnsPayloadOffset = ipHeaderLen + 8
                            val dnsPayloadLen = length - dnsPayloadOffset
                            if (dnsPayloadLen > 12) {
                                val host = extractDnsQuestionHost(buffer, dnsPayloadOffset, dnsPayloadLen)
                                if (host != null) {
                                    val blockedWebsites = db.wellbeingDao().getActiveBlockedWebsites().first()
                                    val settings = settingsStore.settingsFlow.first()
                                    val isAdultBlocked = settings.blockNsfw && AdultContentFilterEngine.isDomainBlocked(host).isBlocked
                                    val matchedRule = WebsiteFilterManager.isHostBlocked(host, blockedWebsites)

                                    if (matchedRule != null || isAdultBlocked) {
                                        // Domain is restricted. Synthesize NXDOMAIN / Refused response
                                        val responsePayload = synthesizeBlockedDnsResponse(buffer, dnsPayloadOffset, dnsPayloadLen)
                                        if (responsePayload != null) {
                                            val fullPacket = buildIpv4UdpPacket(buffer, ipHeaderLen, responsePayload)
                                            outputStream.write(fullPacket)
                                            outputStream.flush()
                                            continue
                                        }
                                    } else {
                                        // Forward to upstream resolver
                                        try {
                                            val dnsQueryBytes = buffer.copyOfRange(dnsPayloadOffset, dnsPayloadOffset + dnsPayloadLen)
                                            val outPacket = DatagramPacket(dnsQueryBytes, dnsQueryBytes.size, upstreamDns, 53)
                                            upstreamSocket.send(outPacket)

                                            val inBuffer = ByteArray(1500)
                                            val inPacket = DatagramPacket(inBuffer, inBuffer.size)
                                            upstreamSocket.receive(inPacket)

                                            val responsePayload = inBuffer.copyOf(inPacket.length)
                                            val fullPacket = buildIpv4UdpPacket(buffer, ipHeaderLen, responsePayload)
                                            outputStream.write(fullPacket)
                                            outputStream.flush()
                                            continue
                                        } catch (_: Exception) {}
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
        } finally {
            try { upstreamSocket.close() } catch (_: Exception) {}
        }
    }

    private fun extractDnsQuestionHost(data: ByteArray, offset: Int, length: Int): String? {
        try {
            var pos = offset + 12 // Skip DNS header (12 bytes)
            val end = offset + length
            val parts = mutableListOf<String>()

            while (pos < end) {
                val len = data[pos].toInt() and 0xFF
                if (len == 0) break
                pos++
                if (pos + len > end) return null
                parts.add(String(data, pos, len, Charsets.US_ASCII))
                pos += len
            }
            return if (parts.isNotEmpty()) parts.joinToString(".") else null
        } catch (_: Exception) {
            return null
        }
    }

    private fun synthesizeBlockedDnsResponse(data: ByteArray, offset: Int, length: Int): ByteArray? {
        if (length < 12) return null
        val response = data.copyOfRange(offset, offset + length)
        // Set QR=1 (response), RCODE=3 (NXDOMAIN), AA=0, TC=0, RA=1
        val flagsHigh = response[2].toInt() and 0xFF
        val flagsLow = response[3].toInt() and 0xFF
        response[2] = (flagsHigh or 0x80).toByte() // QR = 1
        response[3] = ((flagsLow and 0xF0) or 0x03).toByte() // RCODE = 3 (NXDOMAIN)
        return response
    }

    private fun buildIpv4UdpPacket(srcBuffer: ByteArray, ipHeaderLen: Int, udpPayload: ByteArray): ByteArray {
        val totalLength = 20 + 8 + udpPayload.size
        val out = ByteArray(totalLength)

        // Swap src & dst IP
        out[0] = 0x45.toByte() // IPv4, HeaderLen 5 (20 bytes)
        out[1] = 0x00
        out[2] = ((totalLength shr 8) and 0xFF).toByte()
        out[3] = (totalLength and 0xFF).toByte()
        out[4] = 0x00
        out[5] = 0x00
        out[6] = 0x40.toByte() // Don't fragment
        out[7] = 0x00
        out[8] = 64 // TTL
        out[9] = 17 // UDP
        out[10] = 0 // Checksum placeholder
        out[11] = 0

        // Swap IPs (dst -> src, src -> dst)
        System.arraycopy(srcBuffer, 16, out, 12, 4) // Dst IP becomes Src IP
        System.arraycopy(srcBuffer, 12, out, 16, 4) // Src IP becomes Dst IP

        // UDP Header
        val srcPort = ((srcBuffer[ipHeaderLen].toInt() and 0xFF) shl 8) or (srcBuffer[ipHeaderLen + 1].toInt() and 0xFF)
        val dstPort = ((srcBuffer[ipHeaderLen + 2].toInt() and 0xFF) shl 8) or (srcBuffer[ipHeaderLen + 3].toInt() and 0xFF)

        // Swap Ports
        out[20] = ((dstPort shr 8) and 0xFF).toByte()
        out[21] = (dstPort and 0xFF).toByte()
        out[22] = ((srcPort shr 8) and 0xFF).toByte()
        out[23] = (srcPort and 0xFF).toByte()

        val udpLen = 8 + udpPayload.size
        out[24] = ((udpLen shr 8) and 0xFF).toByte()
        out[25] = (udpLen and 0xFF).toByte()
        out[26] = 0 // UDP Checksum optional in IPv4
        out[27] = 0

        // Copy DNS Payload
        System.arraycopy(udpPayload, 0, out, 28, udpPayload.size)
        return out
    }

    private fun stopVpn() {
        isRunning = false
        isVpnRunning = false
        try {
            vpnInterface?.close()
        } catch (_: Exception) {}
        vpnInterface = null
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DNS Distraction Protection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors local domain filters for Kintsu Digital Balance"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("Digital Balance Website Filter")
            .setContentText("Local domain-level protection is active")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }
}
