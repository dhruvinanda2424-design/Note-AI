package com.example.data.remote

import com.example.data.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object SupabaseNotes {
    private const val SUPABASE_URL = "https://ncbfpwjzbbtizksojgzp.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_d3qBi3986meqcK9K0Jngbg_W0MsOXVa"
    
    private val client = OkHttpClient()
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun getNotes(token: String, email: String): Result<List<Note>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$SUPABASE_URL/rest/v1/notes?select=*&user_email=eq.$email&order=timestamp.desc")
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Accept", "application/json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: "[]"
                if (response.isSuccessful) {
                    val notesList = mutableListOf<Note>()
                    val jsonArray = JSONArray(bodyStr)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        notesList.add(
                            Note(
                                id = obj.getInt("id"),
                                userEmail = obj.getString("user_email"),
                                title = obj.getString("title"),
                                content = obj.getString("content"),
                                timestamp = obj.getLong("timestamp"),
                                colorIndex = obj.getInt("color_index"),
                                isArchived = obj.optBoolean("is_archived", false)
                            )
                        )
                    }
                    Result.success(notesList)
                } else {
                    Result.failure(Exception("Failed to fetch notes: ${response.code} ${response.message}"))
                }
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun insertNote(token: String, note: Note): Result<Note> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("user_email", note.userEmail)
                put("title", note.title)
                put("content", note.content)
                put("timestamp", note.timestamp)
                put("color_index", note.colorIndex)
                put("is_archived", note.isArchived)
            }

            val request = Request.Builder()
                .url("$SUPABASE_URL/rest/v1/notes")
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(json.toString().toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val jsonArray = JSONArray(bodyStr)
                    if (jsonArray.length() > 0) {
                        val obj = jsonArray.getJSONObject(0)
                        val savedNote = Note(
                            id = obj.getInt("id"),
                            userEmail = obj.getString("user_email"),
                            title = obj.getString("title"),
                            content = obj.getString("content"),
                            timestamp = obj.getLong("timestamp"),
                            colorIndex = obj.getInt("color_index"),
                            isArchived = obj.optBoolean("is_archived", false)
                        )
                        Result.success(savedNote)
                    } else {
                        Result.success(note)
                    }
                } else {
                    Result.failure(Exception("Failed to insert note on Supabase: ${response.code} $bodyStr"))
                }
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun updateNote(token: String, note: Note): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("title", note.title)
                put("content", note.content)
                put("timestamp", note.timestamp)
                put("color_index", note.colorIndex)
                put("is_archived", note.isArchived)
            }

            val request = Request.Builder()
                .url("$SUPABASE_URL/rest/v1/notes?id=eq.${note.id}")
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .patch(json.toString().toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    val bodyStr = response.body?.string() ?: ""
                    Result.failure(Exception("Failed to update note on Supabase: ${response.code} $bodyStr"))
                }
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun deleteNote(token: String, noteId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$SUPABASE_URL/rest/v1/notes?id=eq.$noteId")
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer $token")
                .delete()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    val bodyStr = response.body?.string() ?: ""
                    Result.failure(Exception("Failed to delete note on Supabase: ${response.code} $bodyStr"))
                }
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
