package com.kintsu.app.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class AuthSession(
    val userId: String,
    val email: String,
    val displayName: String?,
    val accessToken: String,
    val refreshToken: String?,
    val expiresAt: Long = System.currentTimeMillis() + 3600_000L
)

data class SyncMutationPayload(
    val syncUuid: String,
    val tableName: String,
    val operation: String,
    val payloadJson: String,
    val timestamp: Long
)

data class RemoteRecord(
    val syncUuid: String,
    val tableName: String,
    val payloadJson: String,
    val updatedAt: Long,
    val isDeleted: Boolean
)

data class RemoteSyncChanges(
    val records: List<RemoteRecord>,
    val serverTimestamp: Long
)

class KintsuCloudClient(
    private var endpointUrl: String = "https://aspects-oops-lance-limit.trycloudflare.com",
    private var apiKey: String = "",
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
) {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun updateConfig(url: String, key: String) {
        endpointUrl = url.trimEnd('/')
        apiKey = key
    }

    suspend fun signUp(email: String, pass: String, displayName: String?): Result<AuthSession> = withContext(Dispatchers.IO) {
        try {
            val url = "$endpointUrl/auth/v1/signup"
            val bodyObj = JSONObject().apply {
                put("email", email)
                put("password", pass)
                if (!displayName.isNullOrBlank()) {
                    put("data", JSONObject().put("display_name", displayName))
                }
            }
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(bodyObj.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val respStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val msg = parseErrorMessage(respStr, "Sign up failed (${response.code})")
                    return@withContext Result.failure(IOException(msg))
                }
                val json = JSONObject(respStr)
                val user = json.optJSONObject("user") ?: json
                val userId = user.optString("id", java.util.UUID.randomUUID().toString())
                val token = json.optString("access_token", "kintsu_token_${System.currentTimeMillis()}")
                val refreshToken = if (json.has("refresh_token")) json.optString("refresh_token") else null

                Result.success(
                    AuthSession(
                        userId = userId,
                        email = email,
                        displayName = displayName ?: user.optJSONObject("user_metadata")?.optString("display_name"),
                        accessToken = token,
                        refreshToken = refreshToken
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithPassword(email: String, pass: String): Result<AuthSession> = withContext(Dispatchers.IO) {
        try {
            val url = "$endpointUrl/auth/v1/token?grant_type=password"
            val bodyObj = JSONObject().apply {
                put("email", email)
                put("password", pass)
            }
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(bodyObj.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val respStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val msg = parseErrorMessage(respStr, "Invalid email or password (${response.code})")
                    return@withContext Result.failure(IOException(msg))
                }
                val json = JSONObject(respStr)
                val user = json.optJSONObject("user")
                val userId = user?.optString("id") ?: json.optString("id", java.util.UUID.randomUUID().toString())
                val token = json.optString("access_token")
                val refreshToken = if (json.has("refresh_token")) json.optString("refresh_token") else null
                val userMetadata = user?.optJSONObject("user_metadata")
                val name = userMetadata?.optString("display_name")

                Result.success(
                    AuthSession(
                        userId = userId,
                        email = email,
                        displayName = name,
                        accessToken = token,
                        refreshToken = refreshToken
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogleToken(idToken: String, email: String? = null, name: String? = null): Result<AuthSession> = withContext(Dispatchers.IO) {
        try {
            val url = "$endpointUrl/auth/v1/token?grant_type=id_token"
            val bodyObj = JSONObject().apply {
                put("provider", "google")
                put("id_token", idToken)
                if (!email.isNullOrBlank()) put("email", email)
                if (!name.isNullOrBlank()) put("name", name)
            }
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(bodyObj.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val respStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val msg = parseErrorMessage(respStr, "Google sign in failed (${response.code})")
                    return@withContext Result.failure(IOException(msg))
                }
                val json = JSONObject(respStr)
                val user = json.optJSONObject("user")
                val userId = user?.optString("id") ?: json.optString("id")
                val email = user?.optString("email") ?: json.optString("email")
                val token = json.optString("access_token")
                val refreshToken = if (json.has("refresh_token")) json.optString("refresh_token") else null
                val name = user?.optJSONObject("user_metadata")?.optString("full_name")

                Result.success(
                    AuthSession(
                        userId = userId,
                        email = email,
                        displayName = name,
                        accessToken = token,
                        refreshToken = refreshToken
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut(token: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = "$endpointUrl/auth/v1/logout"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer $token")
                .post("{}".toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadMutations(
        token: String,
        userId: String,
        mutations: List<SyncMutationPayload>
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        if (mutations.isEmpty()) return@withContext Result.success(emptyList())
        try {
            val url = "$endpointUrl/rest/v1/sync_records"
            val array = JSONArray()
            mutations.forEach { m ->
                val item = JSONObject().apply {
                    put("user_id", userId)
                    put("sync_uuid", m.syncUuid)
                    put("table_name", m.tableName)
                    put("operation", m.operation)
                    put("payload_json", m.payloadJson)
                    put("updated_at", m.timestamp)
                    put("is_deleted", m.operation == "DELETE")
                }
                array.put(item)
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(array.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful && response.code != 201 && response.code != 204) {
                    val respStr = response.body?.string() ?: ""
                    val msg = parseErrorMessage(respStr, "Sync upload failed (${response.code})")
                    return@withContext Result.failure(IOException(msg))
                }
                Result.success(mutations.map { it.syncUuid })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadChanges(
        token: String,
        userId: String,
        sinceTimestamp: Long
    ): Result<RemoteSyncChanges> = withContext(Dispatchers.IO) {
        try {
            val url = "$endpointUrl/rest/v1/sync_records?user_id=eq.$userId&updated_at=gt.$sinceTimestamp&order=updated_at.asc"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val respStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val msg = parseErrorMessage(respStr, "Sync download failed (${response.code})")
                    return@withContext Result.failure(IOException(msg))
                }
                val array = JSONArray(respStr)
                val list = mutableListOf<RemoteRecord>()
                var latestTime = sinceTimestamp
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val syncUuid = item.getString("sync_uuid")
                    val tableName = item.getString("table_name")
                    val payload = item.optString("payload_json", "{}")
                    val updatedAt = item.optLong("updated_at", System.currentTimeMillis())
                    val isDeleted = item.optBoolean("is_deleted", false)
                    if (updatedAt > latestTime) latestTime = updatedAt
                    list.add(
                        RemoteRecord(
                            syncUuid = syncUuid,
                            tableName = tableName,
                            payloadJson = payload,
                            updatedAt = updatedAt,
                            isDeleted = isDeleted
                        )
                    )
                }
                Result.success(RemoteSyncChanges(records = list, serverTimestamp = latestTime))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCloudData(token: String, userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = "$endpointUrl/rest/v1/sync_records?user_id=eq.$userId"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer $token")
                .delete()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful && response.code != 204) {
                    val respStr = response.body?.string() ?: ""
                    val msg = parseErrorMessage(respStr, "Failed to delete cloud data (${response.code})")
                    return@withContext Result.failure(IOException(msg))
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(jsonStr: String, fallback: String): String {
        return try {
            val json = JSONObject(jsonStr)
            json.optString("message", json.optString("error_description", json.optString("msg", fallback)))
        } catch (_: Exception) {
            if (jsonStr.isNotBlank() && jsonStr.length < 200) jsonStr else fallback
        }
    }
}
