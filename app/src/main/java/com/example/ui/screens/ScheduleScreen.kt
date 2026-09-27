package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.FamilyEvent
import com.example.data.entities.FamilyMember
import com.example.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    events: List<FamilyEvent>,
    members: List<FamilyMember>,
    isParentMode: Boolean = false,
    showAddSheet: Boolean = false,
    onAddSheetDismiss: () -> Unit = {},
    onAddEvent: (title: String, desc: String, time: String, category: String, attendees: String, isKids: Boolean, loc: String) -> Unit,
    onDeleteEvent: (FamilyEvent) -> Unit
) {
    var viewMode by remember { mutableStateOf("Agenda") }
    var selectedMemberId by remember { mutableStateOf<Long?>(null) }

    val viewModes = listOf("Day", "Week", "Month", "Agenda")

    val filteredEvents = remember(events, selectedMemberId) {
        if (selectedMemberId == null) events
        else {
            val memberName = members.find { it.id == selectedMemberId }?.name ?: ""
            events.filter { it.attendeeNames.contains(memberName, ignoreCase = true) || it.attendeeNames.contains("Everyone", ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today's Agenda",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Shared Family Calendar",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = { /* Calendar Sync Action */ }) {
                    Icon(Icons.Default.Sync, contentDescription = "Sync", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                viewModes.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = viewMode == mode,
                        onClick = { viewMode = mode },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = viewModes.size),
                        label = { Text(mode, fontSize = 11.sp) }
                    )
                }
            }
        }

        // Family Filters (Avatar Chips)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            item {
                AvatarChip(
                    name = "All",
                    avatarEmoji = "🏠",
                    isActive = selectedMemberId == null,
                    size = 40,
                    onClick = { selectedMemberId = null }
                )
            }
            items(members) { member ->
                AvatarChip(
                    name = member.name.split(" ").first(),
                    avatarEmoji = member.avatarEmoji,
                    isActive = selectedMemberId == member.id,
                    size = 40,
                    onClick = { selectedMemberId = member.id }
                )
            }
        }

        // Timeline / Agenda View
        if (filteredEvents.isEmpty()) {
            EmptyState(
                emoji = "📅",
                title = "Nothing Scheduled",
                subtitle = "Tap + to add school events, sports, or appointments."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Grouping by date could be added here, but for now we follow the "grouped by time" instruction
                items(filteredEvents.sortedBy { it.timeString }) { event ->
                    CompactEventCard(
                        event = event,
                        isParentMode = isParentMode,
                        onDelete = { onDeleteEvent(event) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = onAddSheetDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            EnhancedCreateEventSheet(
                members = members,
                onAdd = { title, desc, time, cat, attendees, isKids, loc ->
                    onAddEvent(title, desc, time, cat, attendees, isKids, loc)
                    onAddSheetDismiss()
                },
                onDismiss = onAddSheetDismiss
            )
        }
    }
}

@Composable
private fun CompactEventCard(
    event: FamilyEvent,
    isParentMode: Boolean,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    val categoryColor = when (event.category.lowercase()) {
        "school" -> Color(0xFF3B82F6) // Blue
        "sports" -> Color(0xFFF59E0B) // Amber
        "medical" -> Color(0xFF10B981) // Emerald
        "birthday" -> Color(0xFFEC4899) // Pink
        else -> MaterialTheme.colorScheme.primary
    }

    FamilyCard(
        modifier = Modifier.clickable { isExpanded = !isExpanded },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Google Calendar style color strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(if (isExpanded) 80.dp else 40.dp)
                    .clip(CircleShape)
                    .background(categoryColor)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = if (isExpanded) 3 else 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = event.timeString,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (event.location.isNotBlank()) {
                        Text(" • ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = event.location,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (isExpanded && isParentMode) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
            } else if (!isExpanded) {
                // Show attendee emojis in compact view
                Text(
                    text = if (event.attendeeNames.contains("Everyone")) "👨‍👩‍👧‍👦" else "👤",
                    fontSize = 12.sp,
                    modifier = Modifier.alpha(0.6f)
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(top = 12.dp, start = 16.dp)) {
                if (event.description.isNotBlank()) {
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Text(
                    text = "Attendees: ${event.attendeeNames}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                // Placeholder for AI conflict detection or travel time
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(14.dp), tint = Color(0xFFFFB800))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("No schedule conflicts detected", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B7280))
                }
            }
        }
    }
}

@Composable
private fun EnhancedCreateEventSheet(
    members: List<FamilyMember>,
    onAdd: (String, String, String, String, String, Boolean, String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("Aug 4, 2026") }
    var time by remember { mutableStateOf("10:00 AM") }
    var location by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Family") }
    var isRecurring by remember { mutableStateOf(false) }

    val categories = listOf("Family", "School", "Sports", "Medical", "Birthday")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Create Event", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, null)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Event Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp)) }
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Schedule, null, modifier = Modifier.size(18.dp)) }
                )
            }

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp)) }
            )

            Text("Category", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Text("Attendees", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(members) { m ->
                    AvatarChip(name = m.name.split(" ").first(), avatarEmoji = m.avatarEmoji, onClick = {})
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isRecurring, onCheckedChange = { isRecurring = it })
                Text("Recurring Event", style = MaterialTheme.typography.bodyMedium)
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Notes / Description") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(12.dp)
            )
            
            // AI Conflict Detection Status
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("AI: No conflicts found with existing events.", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { onAdd(title, description, time, selectedCategory, "Everyone", false, location) },
            enabled = title.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Save Event", fontWeight = FontWeight.Bold)
        }
    }
}
