package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.FamilyMember
import com.example.data.entities.TaskItem
import com.example.ui.RewardStoreItem
import com.example.ui.RoutineItem
import com.example.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    tasks: List<TaskItem>,
    members: List<FamilyMember>,
    activeMember: FamilyMember? = null,
    isParentMode: Boolean = false,
    showAddSheet: Boolean = false,
    onAddSheetDismiss: () -> Unit = {},
    onSelectActiveMember: (FamilyMember?) -> Unit = {},
    rewardStoreItems: List<RewardStoreItem> = emptyList(),
    routineItems: List<RoutineItem> = emptyList(),
    onAddTask: (String, String, FamilyMember?, Int, String) -> Unit,
    onToggleTaskCompleted: (TaskItem) -> Unit,
    onDeleteTask: (TaskItem) -> Unit,
    onRotateChores: () -> Unit = {},
    onToggleTaskRotation: (TaskItem) -> Unit = {},
    onRedeemReward: (FamilyMember, RewardStoreItem) -> Boolean = { _, _ -> false }
) {
    var selectedFilter by remember { mutableStateOf("Today") }
    var showLeaderboard by remember { mutableStateOf(false) }
    var taskToComplete by remember { mutableStateOf<TaskItem?>(null) }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var confettiSignal by remember { mutableLongStateOf(0L) }

    val filters = listOf("Today", "Upcoming", "Completed", "Family")

    val filteredTasks = remember(tasks, selectedFilter) {
        when (selectedFilter) {
            "Today" -> tasks.filter { !it.isCompleted }
            "Completed" -> tasks.filter { it.isCompleted }
            "Family" -> tasks
            else -> tasks
        }
    }

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
                SectionHeader(
                    title = "Tasks",
                    caption = if (activeMember != null) "Personalized for ${activeMember.name}" else "Daily family goals",
                    action = {
                        IconButton(onClick = { showLeaderboard = true }) {
                            Icon(Icons.Default.EmojiEvents, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            }

            // Dashboard Summary
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard("Streak", "${activeMember?.streak ?: 0} Days", Icons.Default.Whatshot, MaterialTheme.colorScheme.tertiaryContainer)
                    SummaryCard("Progress", "${tasks.count { it.isCompleted }}/${tasks.size}", Icons.Default.DonutLarge, MaterialTheme.colorScheme.secondaryContainer)
                }
            }

            // Filters
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filters) { f ->
                        FilterChip(
                            selected = selectedFilter == f,
                            onClick = { selectedFilter = f },
                            label = { Text(f) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            if (filteredTasks.isEmpty()) {
                item {
                    EmptyState(emoji = "🎯", title = "All clear!", subtitle = "No tasks to show for $selectedFilter.")
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    ChecklistItem(
                        title = task.title,
                        subtitle = "Assigned: ${task.assignedMemberName}",
                        isCompleted = task.isCompleted,
                        onCheckedChange = {
                            if (!task.isCompleted) {
                                if (activeMember == null && task.assignedMemberId == 0L) {
                                    taskToComplete = task
                                } else {
                                    confettiSignal = System.currentTimeMillis()
                                    onToggleTaskCompleted(task)
                                }
                            } else {
                                onToggleTaskCompleted(task)
                            }
                        },
                        trailing = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${task.rewardPoints}⭐", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                // Protect delete action
                                if (isParentMode) {
                                    IconButton(onClick = { onDeleteTask(task) }) {
                                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(100.dp).navigationBarsPadding()) }
        }
    }

    if (taskToComplete != null) {
        ModalBottomSheet(onDismissRequest = { taskToComplete = null }) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Who is completing this task?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(members) { m ->
                        AvatarChip(name = m.name, avatarEmoji = m.avatarEmoji, size = 64, onClick = {
                            onToggleTaskCompleted(taskToComplete!!)
                            confettiSignal = System.currentTimeMillis()
                            taskToComplete = null
                        })
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(onDismissRequest = onAddSheetDismiss, sheetState = sheetState) {
            AddTaskSheetContent(
                members = members,
                onAddTask = { title, desc, member, pts, cat ->
                    onAddTask(title, desc, member, pts, cat)
                    onAddSheetDismiss()
                }
            )
        }
    }

    if (showLeaderboard) {
        ModalBottomSheet(onDismissRequest = { showLeaderboard = false }) {
            LeaderboardSheetContent(members = members)
        }
    }

    ConfettiOverlay(triggerSignal = confettiSignal, message = "🎉 Task Done! ⭐")
}

@Composable
private fun RowScope.SummaryCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Surface(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(18.dp),
        color = color.copy(alpha = 0.4f)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall)
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AddTaskSheetContent(
    members: List<FamilyMember>,
    onAddTask: (String, String, FamilyMember?, Int, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedMember by remember { mutableStateOf(members.firstOrNull()) }
    var points by remember { mutableFloatStateOf(15f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f)
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Text("New Hub Task", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("What needs to be done?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Details") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Text("Assign to", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(members) { m ->
                    AvatarChip(name = m.name.split(" ").first(), avatarEmoji = m.avatarEmoji, isActive = selectedMember?.id == m.id, onClick = { selectedMember = m })
                }
            }
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Reward", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text("${points.toInt()} Stars ⭐", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                Slider(value = points, onValueChange = { points = it }, valueRange = 5f..100f, steps = 19)
            }
        }
        
        Button(
            onClick = { onAddTask(title, description, selectedMember, points.toInt(), "General") },
            enabled = title.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Create Task", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LeaderboardSheetContent(members: List<FamilyMember>) {
    Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp)) {
        Text("Star Leaderboard", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Total family contributions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        members.sortedByDescending { it.points }.forEachIndexed { index, member ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("#${index + 1}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
                AvatarChip(name = "", avatarEmoji = member.avatarEmoji, size = 40, onClick = {})
                Spacer(modifier = Modifier.width(12.dp))
                Text(member.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("${member.points}⭐", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            if (index < members.size - 1) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
