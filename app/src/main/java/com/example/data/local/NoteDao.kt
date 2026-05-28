package com.example.data.local

import androidx.room.*
import com.example.data.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE userEmail = :email ORDER BY timestamp DESC")
    fun getNotesByUser(email: String): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Int)

    @Query("DELETE FROM notes WHERE userEmail = :email")
    suspend fun deleteNotesByUser(email: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<Note>)

    @Transaction
    suspend fun syncNotes(userEmail: String, remoteNotes: List<Note>) {
        deleteNotesByUser(userEmail)
        insertNotes(remoteNotes)
    }
}
