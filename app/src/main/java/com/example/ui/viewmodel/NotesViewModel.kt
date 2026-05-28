package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Note
import com.example.data.repository.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotesViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _currentUserEmail = MutableStateFlow("")
    val currentUserEmail = _currentUserEmail.asStateFlow()

    // Notes List State
    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<Note>> = _currentUserEmail
        .flatMapLatest { email ->
            if (email.isEmpty()) {
                flowOf(emptyList())
            } else {
                noteRepository.getNotes(email)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filter/Search State
    val searchQuery = MutableStateFlow("")
    val isGridLayout = MutableStateFlow(true) // Display layout preferences

    // Add Note Form State
    val noteTitle = MutableStateFlow("")
    val noteContent = MutableStateFlow("")
    val selectedColorIndex = MutableStateFlow(0) // Index into custom note colors

    // Filtered Notes
    val filteredNotes: StateFlow<List<Note>> = combine(notes, searchQuery) { notesList, query ->
        if (query.isBlank()) {
            notesList
        } else {
            notesList.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setUserEmail(email: String) {
        _currentUserEmail.value = email
    }

    fun addNote() {
        val email = _currentUserEmail.value
        val title = noteTitle.value.trim()
        val content = noteContent.value.trim()
        val colorIdx = selectedColorIndex.value

        if (email.isEmpty() || (title.isEmpty() && content.isEmpty())) return

        viewModelScope.launch {
            val note = Note(
                userEmail = email,
                title = if (title.isEmpty()) "Untitled Note" else title,
                content = content,
                colorIndex = colorIdx
            )
            noteRepository.insert(note)
            
            // Clear inputs
            noteTitle.value = ""
            noteContent.value = ""
            selectedColorIndex.value = 0
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.delete(note)
        }
    }

    fun toggleLayout() {
        isGridLayout.value = !isGridLayout.value
    }
}

@Suppress("UNCHECKED_CAST")
class NotesViewModelFactory(
    private val noteRepository: NoteRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            return NotesViewModel(noteRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
