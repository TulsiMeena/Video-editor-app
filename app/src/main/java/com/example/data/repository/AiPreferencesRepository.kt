package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lumina_ai_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_PERFORMANCE_MODE = "performance_mode"
    }

    fun getStoredApiKey(): String {
        val stored = prefs.getString(KEY_GEMINI_API_KEY, "") ?: ""
        if (stored.isNotBlank()) return stored
        
        // Fallback to BuildConfig if provided in env
        return try {
            val buildKey = BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String ?: ""
            buildKey
        } catch (e: Exception) {
            ""
        }
    }

    fun setApiKey(key: String) {
        prefs.edit().putString(KEY_GEMINI_API_KEY, key.trim()).apply()
    }

    fun clearApiKey() {
        prefs.edit().remove(KEY_GEMINI_API_KEY).apply()
    }

    fun isApiConfigured(): Boolean {
        return getStoredApiKey().isNotBlank()
    }

    fun setPerformanceMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PERFORMANCE_MODE, enabled).apply()
    }

    fun isPerformanceMode(): Boolean {
        return prefs.getBoolean(KEY_PERFORMANCE_MODE, false)
    }

    /**
     * Tests the API connection by making a tiny REST call to Gemini 3.5 Flash.
     */
    suspend fun testConnection(apiKeyOverride: String? = null): Result<Boolean> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyOverride ?: getStoredApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("API Key is empty."))
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val jsonPayload = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", "Ping"))
                    })
                })
            })
        }

        val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    val code = response.code
                    val msg = when (code) {
                        400 -> "Invalid API Key or Bad Request ($code)"
                        403 -> "API Key quota exceeded or permission denied ($code)"
                        else -> "API Error Code: $code"
                    }
                    Result.failure(Exception(msg))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message ?: "Could not reach Gemini service"}"))
        }
    }
}
