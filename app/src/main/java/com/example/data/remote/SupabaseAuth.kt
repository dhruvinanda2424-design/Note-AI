package com.example.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SupabaseAuth {
    private const val SUPABASE_URL = "https://ncbfpwjzbbtizksojgzp.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_d3qBi3986meqcK9K0Jngbg_W0MsOXVa"
    
    private val client = OkHttpClient()
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun signUp(email: String, password: String, name: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("email", email)
                put("password", password)
                put("data", JSONObject().apply {
                    put("name", name)
                })
            }

            val request = Request.Builder()
                .url("$SUPABASE_URL/auth/v1/signup")
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .post(json.toString().toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Result.success("Sign up successful!")
                } else {
                    val errorMsg = try {
                        val obj = JSONObject(bodyStr)
                        obj.optString("error_description", "").ifEmpty {
                            obj.optString("msg", "").ifEmpty {
                                obj.optString("error", "Error code: ${response.code}")
                            }
                        }
                    } catch (e: Exception) {
                        "Error code: ${response.code}"
                    }
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Throwable) {
            Result.failure(Exception(e.message ?: "Network error", e))
        }
    }

    suspend fun signIn(email: String, password: String): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("email", email)
                put("password", password)
            }

            val request = Request.Builder()
                .url("$SUPABASE_URL/auth/v1/token?grant_type=password")
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .post(json.toString().toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val jsonObj = JSONObject(bodyStr)
                    val accessToken = jsonObj.optString("access_token", "")
                    val userObj = jsonObj.optJSONObject("user")
                    val metadata = userObj?.optJSONObject("user_metadata")
                    val name = metadata?.optString("name", "") ?: email.substringBefore("@")
                    Result.success(Pair(accessToken, name))
                } else {
                    val errorMsg = try {
                        val obj = JSONObject(bodyStr)
                        obj.optString("error_description", "").ifEmpty {
                            obj.optString("msg", "").ifEmpty {
                                obj.optString("error", "Error code: ${response.code}")
                            }
                        }
                    } catch (e: Exception) {
                        "Error code: ${response.code}"
                    }
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Throwable) {
            Result.failure(Exception(e.message ?: "Network error", e))
        }
    }
}
