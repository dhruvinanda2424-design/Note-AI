package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Note
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.NotesViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel,
    notesViewModel: NotesViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddNoteDialog by remember { mutableStateOf(false) }
    val notes by notesViewModel.filteredNotes.collectAsState()
    val searchQuery by notesViewModel.searchQuery.collectAsState()
    val isGridLayout by notesViewModel.isGridLayout.collectAsState()

    val userName = authViewModel.getCurrentUserName()
    val userEmail = authViewModel.getCurrentUserEmail()

    // Pass the user context to the NotesViewModel to retrieve the relevant Room details
    LaunchedEffect(userEmail) {
        notesViewModel.setUserEmail(userEmail)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NoteAlt,
                            contentDescription = "Notes Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp).padding(end = 6.dp)
                        )
                        Text(
                            text = "My Notes Workspace",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                },
                actions = {
                    // Sign out option
                    IconButton(
                        onClick = {
                            authViewModel.logout()
                            onLogout()
                        },
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Log Out Button",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddNoteDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .testTag("add_note_fab")
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Note Button"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Personalized User Info Hero Card with profile details
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hello, $userName 👋",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        color = Color.Transparent
                    ) {
                        Text(
                            text = "ACTIVE SESSION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Quick Actions: Search Engine + Layout Toggle switches
            val searchBgColor = if (isSystemInDarkTheme()) Color(0xFF2E3033) else Color(0xFFEEF0F8)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { notesViewModel.searchQuery.value = it },
                    placeholder = { Text("Search title or description...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("search_bar"),
                    shape = RoundedCornerShape(26.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = searchBgColor,
                        unfocusedContainerColor = searchBgColor,
                        disabledContainerColor = searchBgColor,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = { notesViewModel.toggleLayout() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            CircleShape
                        )
                        .testTag("layout_toggle")
                ) {
                    Icon(
                        imageVector = if (isGridLayout) Icons.AutoMirrored.Filled.FormatListBulleted else Icons.Default.GridView,
                        contentDescription = "Toggle Grid/List layout"
                    )
                }
            }

            // Active list of filtered notes or standard empty indicator layout state
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                            contentDescription = "No notes image icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(72.dp)
                                .padding(bottom = 12.dp)
                        )
                        Text(
                            text = if (searchQuery.isBlank()) "No notes created yet" else "No matching results found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (searchQuery.isBlank()) "Tap + to add your very first personal notes." else "Try adjusting your search terms.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                if (isGridLayout) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f).testTag("notes_grid")
                    ) {
                        items(notes, key = { it.id }) { note ->
                            NoteCardItem(
                                note = note,
                                onDelete = { notesViewModel.deleteNote(note) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f).testTag("notes_list")
                    ) {
                        items(notes, key = { it.id }) { note ->
                            NoteCardItem(
                                note = note,
                                onDelete = { notesViewModel.deleteNote(note) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal dialog trigger for writing notes
    if (showAddNoteDialog) {
        val darkTheme = isSystemInDarkTheme()
        val noteTitleInput by notesViewModel.noteTitle.collectAsState()
        val noteContentInput by notesViewModel.noteContent.collectAsState()
        val colorIndexSelected by notesViewModel.selectedColorIndex.collectAsState()

        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = {
                Text(
                    text = "New Note Entry",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                val inputBgColor = if (darkTheme) Color(0xFF2E3033) else Color(0xFFEEF0F8)
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = noteTitleInput,
                        onValueChange = { notesViewModel.noteTitle.value = it },
                        label = { Text("Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_note_title_input"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = inputBgColor,
                            unfocusedContainerColor = inputBgColor,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = noteContentInput,
                        onValueChange = { notesViewModel.noteContent.value = it },
                        label = { Text("Start typing notes here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("new_note_content_input"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = inputBgColor,
                            unfocusedContainerColor = inputBgColor,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Customize Palette",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Color block choices
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val colorChoices = listOf(
                            Pair(NoteAmberLight, NoteAmberDark),
                            Pair(NoteEmeraldLight, NoteEmeraldDark),
                            Pair(NoteRoseLight, NoteRoseDark),
                            Pair(NoteBlueLight, NoteBlueDark),
                            Pair(NoteVioletLight, NoteVioletDark)
                        )

                        colorChoices.forEachIndexed { i, colors ->
                            val isSelected = colorIndexSelected == i
                            val displayBg = if (darkTheme) colors.second else colors.first
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(displayBg)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .clickable { notesViewModel.selectedColorIndex.value = i }
                                    .testTag("color_choice_$i"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected color",
                                        modifier = Modifier.size(16.dp),
                                        tint = if (darkTheme) Color.White else Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        notesViewModel.addNote()
                        showAddNoteDialog = false
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("save_note_button")
                ) {
                    Text("Save Note")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddNoteDialog = false },
                    modifier = Modifier.testTag("cancel_note_button")
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun NoteCardItem(
    note: Note,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val darkTheme = isSystemInDarkTheme()
    val colorChoices = listOf(
        Pair(NoteAmberLight, NoteAmberDark),
        Pair(NoteEmeraldLight, NoteEmeraldDark),
        Pair(NoteRoseLight, NoteRoseDark),
        Pair(NoteBlueLight, NoteBlueDark),
        Pair(NoteVioletLight, NoteVioletDark)
    )

    val colorIndices = note.colorIndex.coerceIn(0, 4)
    val chosenPair = colorChoices[colorIndices]
    val bgCardColor = if (darkTheme) chosenPair.second else chosenPair.first
    val txtHeaderColor = if (darkTheme) Color.White else Color.Black
    val txtBodyColor = if (darkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)

    val formattedDate = remember(note.timestamp) {
        val sdf = SimpleDateFormat("MMM d, yyyy • hh:mm a", Locale.getDefault())
        sdf.format(Date(note.timestamp))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("note_item_${note.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgCardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = txtHeaderColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).testTag("note_title_${note.id}")
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_note_button_${note.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Note",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = txtBodyColor,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 40.dp)
                    .testTag("note_content_${note.id}")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall,
                color = txtBodyColor.copy(alpha = 0.5f)
            )
        }
    }
}
