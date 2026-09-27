package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.FamilyMember
import com.example.ui.components.FamilyAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMembersDialog(
    members: List<FamilyMember>,
    onAddMember: (name: String, role: String, emoji: String, colorHex: String, bdayMonth: Int, bdayDay: Int, bdayYear: Int, photoUri: String?) -> Unit,
    onDeleteMember: (FamilyMember) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Kid") }
    var selectedEmoji by remember { mutableStateOf("👦") }
    var photoUriString by remember { mutableStateOf<String?>(null) }

    // Birthday selection state
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { photoUriString = it.toString() }
    }

    val emojis = listOf("👨", "👩", "👧", "👦", "👵", "👴", "👶", "🐶", "🐱", "🐰")
    val roles = listOf("Parent", "Kid", "Teen", "Grandparent", "Pet")

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Family Members & Birthdays 🎂", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("members_dialog_content"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!showAddForm) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(members) { m ->
                            val daysToBday = com.example.util.BirthdayUtil.getDaysUntilBirthday(m)
                            val age = m.getAge()
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        FamilyAvatar(
                                            avatarEmoji = m.avatarEmoji,
                                            photoUri = m.photoUri,
                                            size = 40.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(m.name, fontWeight = FontWeight.Bold)
                                            val roleText = if (age != null) "${m.role} ($age yo)" else m.role
                                            Text("$roleText • ${m.points} Stars ⭐", style = MaterialTheme.typography.bodySmall)
                                            if (daysToBday != null) {
                                                Text(
                                                    text = if (daysToBday == 0) "🎉 TODAY IS BIRTHDAY! 🎈" else "🎂 Birthday in $daysToBday days",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }

                                    IconButton(onClick = { onDeleteMember(m) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showAddForm = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add New Family Member")
                    }
                } else {
                    // Profile Photo Upload / Avatar Selection
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FamilyAvatar(
                            avatarEmoji = selectedEmoji,
                            photoUri = photoUriString,
                            size = 54.dp,
                            showEditBadge = true,
                            onEditClick = { imagePickerLauncher.launch("image/*") }
                        )

                        Column {
                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (photoUriString == null) "📷 Upload Photo" else "📷 Change Photo", fontSize = 12.sp)
                            }
                            if (photoUriString != null) {
                                TextButton(onClick = { photoUriString = null }) {
                                    Text("Remove Photo", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Member Name *") },
                        placeholder = { Text("e.g. Grandma Rose") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )

                    Text("Role:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(roles) { r ->
                            FilterChip(
                                selected = role == r,
                                onClick = { role = r },
                                label = { Text(r) }
                            )
                        }
                    }

                    Text("Choose Avatar Emoji:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(emojis) { em ->
                            Surface(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { selectedEmoji = em }
                                    .padding(4.dp),
                                color = if (selectedEmoji == em && photoUriString == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ) {
                                Text(em, fontSize = 24.sp, modifier = Modifier.padding(4.dp))
                            }
                        }
                    }

                    Text("Birthday Date (Optional):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    
                    val dateText = selectedDateMillis?.let {
                        val cal = java.util.Calendar.getInstance().apply { timeInMillis = it }
                        "${cal.get(java.util.Calendar.MONTH) + 1}/${cal.get(java.util.Calendar.DAY_OF_MONTH)}/${cal.get(java.util.Calendar.YEAR)}"
                    } ?: "Tap to select birthday"

                    OutlinedTextField(
                        value = dateText,
                        onValueChange = {},
                        label = { Text("Birthday") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddForm = false }) {
                            Text("Back")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val cal = java.util.Calendar.getInstance()
                                    selectedDateMillis?.let { cal.timeInMillis = it }
                                    
                                    val bm = if (selectedDateMillis != null) cal.get(java.util.Calendar.MONTH) + 1 else 0
                                    val bd = if (selectedDateMillis != null) cal.get(java.util.Calendar.DAY_OF_MONTH) else 0
                                    val by = if (selectedDateMillis != null) cal.get(java.util.Calendar.YEAR) else 0
                                    
                                    onAddMember(name, role, selectedEmoji, "#E05A47", bm, bd, by, photoUriString)
                                    name = ""
                                    photoUriString = null
                                    selectedDateMillis = null
                                    showAddForm = false
                                }
                            },
                            enabled = name.isNotBlank()
                        ) {
                            Text("Save Member")
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!showAddForm) {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}
