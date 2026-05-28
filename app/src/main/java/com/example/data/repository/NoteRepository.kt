package com.example.data.repository

import com.example.data.local.NoteDao
import com.example.data.local.SessionManager
import com.example.data.model.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteRepository(
    private val noteDao: NoteDao,
    private val sessionManager: SessionManager
) {

    fun getNotes(userEmail: String): Flow<List<Note>> {
        val token = sessionManager.getJwtToken()
        if (!token.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                try {
                    val result = com.example.data.remote.SupabaseNotes.getNotes(token, userEmail)
                    result.onSuccess { remoteNotes ->
                        noteDao.syncNotes(userEmail, remoteNotes)
                    }
                } catch (e: Throwable) {
                    // Suppress all background sync errors to remain offline resilient
                }
            }
        }
        return noteDao.getNotesByUser(userEmail)
    }

    suspend fun insert(note: Note) = withContext(Dispatchers.IO) {
        val token = sessionManager.getJwtToken()
        if (!token.isNullOrEmpty()) {
            val result = com.example.data.remote.SupabaseNotes.insertNote(token, note)
            result.onSuccess { savedNote ->
                noteDao.insertNote(savedNote)
            }.onFailure {
                noteDao.insertNote(note)
            }
        } else {
            noteDao.insertNote(note)
        }
    }

    suspend fun update(note: Note) = withContext(Dispatchers.IO) {
        val token = sessionManager.getJwtToken()
        if (!token.isNullOrEmpty()) {
            com.example.data.remote.SupabaseNotes.updateNote(token, note)
        }
        noteDao.insertNote(note)
    }

    suspend fun delete(note: Note) = withContext(Dispatchers.IO) {
        val token = sessionManager.getJwtToken()
        if (!token.isNullOrEmpty()) {
            com.example.data.remote.SupabaseNotes.deleteNote(token, note.id)
        }
        noteDao.deleteNote(note)
    }

    suspend fun deleteById(noteId: Int) = withContext(Dispatchers.IO) {
        val token = sessionManager.getJwtToken()
        if (!token.isNullOrEmpty()) {
            com.example.data.remote.SupabaseNotes.deleteNote(token, noteId)
        }
        noteDao.deleteNoteById(noteId)
    }
}
