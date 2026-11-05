package com.kintsu.app.wellbeing.ui

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

/**
 * Floating luxury pill overlay displaying real-time countdowns for:
 * 1. Active Emergency Passes (e.g. 5m emergency reel/app passes)
 * 2. Approaching Limits (e.g. within 2 minutes of app, group, shorts, or session limits)
 *
 * Designed with Kintsu ceramic/gold minimalism, zero distraction, and full touch-drag mobility.
 */
class KintsuFloatingPillOverlay(
    private val service: AccessibilityService
) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val windowManager = service.getSystemService(Context.WINDOW_SERVICE) as? WindowManager

    private var pillContainer: LinearLayout? = null
    private var dotIndicator: View? = null
    private var textLabel: TextView? = null
    private var isViewAttached = false

    private val windowParams: WindowManager.LayoutParams by lazy {
        val density = service.resources.displayMetrics.density
        val topMarginPx = (46 * density).toInt()

        WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = topMarginPx
        }
    }

    private var lastMode: String? = null
    private var lastFormattedTime: String? = null

    init {
        mainHandler.post {
            createPillView()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createPillView() {
        if (pillContainer != null) return
        val density = service.resources.displayMetrics.density

        val container = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val padH = (14 * density).toInt()
            val padV = (6 * density).toInt()
            setPadding(padH, padV, padH, padV)
            elevation = 12 * density
        }

        // Pill Obsidian Glass Background with Gold Seam Border
        val bgDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 999f * density
            setColor(0xF0141210.toInt()) // Deep obsidian charcoal with 94% opacity
            setStroke((1.4f * density).toInt(), 0xFFD4AF37.toInt()) // Kintsu Gold Seam
        }
        container.background = bgDrawable

        // Dot indicator
        val dot = View(service).apply {
            val dotSize = (7 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(dotSize, dotSize).apply {
                marginEnd = (8 * density).toInt()
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0xFFD4AF37.toInt())
            }
        }

        // Pulse animation on the dot
        val pulse = AlphaAnimation(0.35f, 1.0f).apply {
            duration = 1000
            repeatMode = Animation.REVERSE
            repeatCount = Animation.INFINITE
        }
        dot.startAnimation(pulse)
        container.addView(dot)

        // Text label
        val tv = TextView(service).apply {
            setTextColor(0xFFF5F2EB.toInt()) // Kintsu Ivory Cream
            textSize = 12.5f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            includeFontPadding = false
            text = ""
        }
        container.addView(tv)

        // Draggable mobility
        var initialX = 0
        var initialY = 0
        var touchStartX = 0f
        var touchStartY = 0f

        container.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = windowParams.x
                    initialY = windowParams.y
                    touchStartX = event.rawX
                    touchStartY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    windowParams.x = initialX + (event.rawX - touchStartX).toInt()
                    windowParams.y = initialY + (event.rawY - touchStartY).toInt()
                    try {
                        windowManager?.updateViewLayout(container, windowParams)
                    } catch (_: Exception) {}
                    true
                }
                else -> false
            }
        }

        pillContainer = container
        dotIndicator = dot
        textLabel = tv
    }

    /**
     * Displays the emergency pass countdown pill on screen.
     */
    fun showEmergencyPass(packageName: String, remainingMillis: Long) {
        val totalSeconds = (remainingMillis / 1000L).coerceAtLeast(0L)
        val mins = totalSeconds / 60
        val secs = totalSeconds % 60
        val timeFormatted = String.format(Locale.US, "%02d:%02d", mins, secs)

        mainHandler.post {
            createPillView()
            val container = pillContainer ?: return@post
            val tv = textLabel ?: return@post
            val dot = dotIndicator ?: return@post

            val displayText = "Pass  $timeFormatted"
            if (lastFormattedTime != displayText || lastMode != "EMERGENCY") {
                lastFormattedTime = displayText
                lastMode = "EMERGENCY"
                tv.text = displayText

                // Gold Seam theme for Emergency pass
                val density = service.resources.displayMetrics.density
                val bg = container.background as? GradientDrawable
                bg?.setStroke((1.4f * density).toInt(), 0xFFD4AF37.toInt())
                (dot.background as? GradientDrawable)?.setColor(0xFFD4AF37.toInt())
            }

            attachIfNeeded(container)
        }
    }

    /**
     * Displays the approaching limit warning pill on screen.
     */
    fun showApproachingLimit(label: String, remainingSeconds: Long) {
        val totalSec = remainingSeconds.coerceAtLeast(0L)
        val mins = totalSec / 60
        val secs = totalSec % 60
        val timeFormatted = String.format(Locale.US, "%02d:%02d", mins, secs)

        mainHandler.post {
            createPillView()
            val container = pillContainer ?: return@post
            val tv = textLabel ?: return@post
            val dot = dotIndicator ?: return@post

            val cleanLabel = if (label.length > 12) label.take(10) + ".." else label
            val displayText = "$cleanLabel  $timeFormatted"

            if (lastFormattedTime != displayText || lastMode != "LIMIT") {
                lastFormattedTime = displayText
                lastMode = "LIMIT"
                tv.text = displayText

                // Amber Gold Accent for approaching limits
                val density = service.resources.displayMetrics.density
                val bg = container.background as? GradientDrawable
                bg?.setStroke((1.4f * density).toInt(), 0xFFE6A23C.toInt())
                (dot.background as? GradientDrawable)?.setColor(0xFFE6A23C.toInt())
            }

            attachIfNeeded(container)
        }
    }

    /**
     * Displays an active micro-pass intentional timer on screen.
     */
    fun showMicroPass(label: String, remainingSeconds: Long) {
        val totalSec = remainingSeconds.coerceAtLeast(0L)
        val mins = totalSec / 60
        val secs = totalSec % 60
        val timeFormatted = String.format(Locale.US, "%02d:%02d", mins, secs)

        mainHandler.post {
            createPillView()
            val container = pillContainer ?: return@post
            val tv = textLabel ?: return@post
            val dot = dotIndicator ?: return@post

            val cleanLabel = if (label.length > 10) label.take(8) + ".." else label
            val displayText = "$cleanLabel  $timeFormatted"

            if (lastFormattedTime != displayText || lastMode != "MICRO_PASS") {
                lastFormattedTime = displayText
                lastMode = "MICRO_PASS"
                tv.text = displayText

                val density = service.resources.displayMetrics.density
                val bg = container.background as? GradientDrawable
                bg?.setStroke((1.4f * density).toInt(), 0xFFD4AF37.toInt())
                (dot.background as? GradientDrawable)?.setColor(0xFFD4AF37.toInt())
            }

            attachIfNeeded(container)
        }
    }

    /**
     * Hides and detaches the floating pill overlay.
     */
    fun hide() {
        mainHandler.post {
            val container = pillContainer ?: return@post
            if (isViewAttached) {
                try {
                    windowManager?.removeView(container)
                } catch (_: Exception) {}
                isViewAttached = false
                lastMode = null
                lastFormattedTime = null
            }
        }
    }

    private fun attachIfNeeded(view: View) {
        if (!isViewAttached) {
            try {
                windowManager?.addView(view, windowParams)
                isViewAttached = true
            } catch (_: Exception) {}
        }
    }

    fun onDestroy() {
        hide()
        pillContainer = null
        dotIndicator = null
        textLabel = null
    }
}
