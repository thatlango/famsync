package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.FamilyMember
import com.example.data.entities.MoodCheckIn
import com.example.ui.components.*

@Composable
fun MoodScreen(
    members: List<FamilyMember>,
    moods: List<MoodCheckIn>,
    activeMember: FamilyMember? = null,
    isParentMode: Boolean = false,
    onRecordMood: (member: FamilyMember, moodEmoji: String, moodLabel: String, note: String) -> Unit,
    onDeleteMood: (MoodCheckIn) -> Unit
) {
    var selectedMember by remember(members, activeMember) { mutableStateOf(activeMember ?: members.firstOrNull()) }
    var selectedMood by remember { mutableStateOf(defaultMoodGridList[0]) }
    var noteText by remember { mutableStateOf("") }
    var confettiSignal by remember { mutableLongStateOf(0L) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionHeader(title = "Mood Board", caption = "How is everyone doing today?")
            }

            // Step 1 & 2: Record Mood Card
            item {
                FamilyCard {
                    Text("1. Choose family member", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(members) { member ->
                            AvatarChip(
                                name = member.name.split(" ").first(),
                                avatarEmoji = member.avatarEmoji,
                                isActive = selectedMember?.id == member.id,
                                size = 48,
                                onClick = { selectedMember = member }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("2. Select your mood", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Compact 4-column Grid
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        defaultMoodGridList.chunked(4).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { mood ->
                                    val isSelected = selectedMood.label == mood.label
                                    Surface(
                                        onClick = { selectedMood = mood },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) mood.containerColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, mood.textColor) else null
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(mood.emoji, fontSize = 24.sp)
                                            Text(mood.label, style = MaterialTheme.typography.labelSmall, color = if (isSelected) mood.textColor else MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                                // Fill remaining space if row is not full
                                if (row.size < 4) {
                                    repeat(4 - row.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("3. Add a note (Optional)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        placeholder = { Text("What's on your mind?") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            selectedMember?.let { 
                                onRecordMood(it, selectedMood.emoji, selectedMood.label, noteText)
                                noteText = ""
                                confettiSignal = System.currentTimeMillis()
                            }
                        },
                        enabled = selectedMember != null,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Save Check-in", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Family History
            item {
                SectionHeader(title = "Recent Check-ins")
            }

            if (moods.isEmpty()) {
                item {
                    EmptyState(emoji = "😊", title = "No entries yet", subtitle = "Be the first to check in!")
                }
            } else {
                items(moods) { mood ->
                    FamilyCard(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(mood.moodEmoji, fontSize = 20.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(mood.memberName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                if (mood.note.isNotBlank()) {
                                    Text(mood.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (isParentMode) {
                                IconButton(onClick = { onDeleteMood(mood) }) {
                                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        ConfettiOverlay(triggerSignal = confettiSignal, message = "🎉 Mood Logged!")
    }
}
