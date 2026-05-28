package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.data.local.AppDatabase
import com.example.data.local.SessionManager
import com.example.data.repository.NoteRepository
import com.example.data.repository.UserRepository
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.AuthViewModelFactory
import com.example.ui.viewmodel.NotesViewModel
import com.example.ui.viewmodel.NotesViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize dependencies
        val database = AppDatabase.getInstance(applicationContext)
        val userRepository = UserRepository(database.userDao())
        val sessionManager = SessionManager(applicationContext)
        val noteRepository = NoteRepository(database.noteDao(), sessionManager)

        // Instantiate ViewModels using their Factories
        val authViewModel: AuthViewModel by viewModels {
            AuthViewModelFactory(userRepository, sessionManager)
        }
        val notesViewModel: NotesViewModel by viewModels {
            NotesViewModelFactory(noteRepository)
        }

        enableEdgeToEdge()

        setContent {
            val systemIsDark = isSystemInDarkTheme()
            LaunchedEffect(systemIsDark) {
                authViewModel.initTheme(systemIsDark)
            }
            val darkThemeState by authViewModel.isDarkMode.collectAsState()
            val isDarkTheme = darkThemeState ?: systemIsDark

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        authViewModel = authViewModel,
                        notesViewModel = notesViewModel
                    )
                }
            }
        }
    }
}
