package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.FamilyEvent
import com.example.data.entities.FamilyMember
import com.example.data.entities.TaskItem
import com.example.ui.MealPlanItem
import java.text.SimpleDateFormat
import java.util.*

data class DashboardActionItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: Color)

@Composable
fun QuickActionsRow(
    onAction: (String) -> Unit
) {
    val actions = listOf(
        DashboardActionItem("Add Task", Icons.Default.AddTask, MaterialTheme.colorScheme.primaryContainer),
        DashboardActionItem("Update Mood", Icons.Default.EmojiEmotions, MaterialTheme.colorScheme.secondaryContainer),
        DashboardActionItem("Add Event", Icons.Default.Event, MaterialTheme.colorScheme.tertiaryContainer),
        DashboardActionItem("Meal Plan", Icons.Default.Restaurant, MaterialTheme.colorScheme.surfaceVariant),
        DashboardActionItem("Family Chat", Icons.Default.ChatBubble, MaterialTheme.colorScheme.primaryContainer),
        DashboardActionItem("Add Memory", Icons.Default.CameraAlt, MaterialTheme.colorScheme.secondaryContainer),
        DashboardActionItem("Shopping", Icons.Default.ShoppingCart, MaterialTheme.colorScheme.tertiaryContainer)
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(actions) { item ->
            Surface(
                onClick = { onAction(item.label) },
                modifier = Modifier.height(80.dp).width(160.dp),
                shape = RoundedCornerShape(24.dp),
                color = item.color,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(item.icon, null, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(item.label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TodaySummaryStrip(
    tasksRemaining: Int,
    eventsCount: Int,
    dinnerMeal: String?,
    moodStatus: String,
    streak: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TODAY AT A GLANCE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            
            SummaryItem("✅ $tasksRemaining chores left")
            SummaryItem("📅 $eventsCount events")
            SummaryItem("🍽 $dinnerMeal")
            SummaryItem("😊 $moodStatus")
            SummaryItem("🏆 $streak day streak")
        }
    }
}

@Composable
private fun SummaryItem(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
fun DashboardWidgetGrid(
    events: List<FamilyEvent>,
    tasks: List<TaskItem>,
    meals: List<MealPlanItem>,
    members: List<FamilyMember>,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { ScheduleWidget(events) }
        item { ChoresWidget(tasks) }
        item { MealsWidget(meals) }
        item { MoodWidget(members) }
        item { ShoppingWidget() }
        item { FamilyProgressWidget() }
        item { AchievementWidget() }
        item { WeatherWidget() }
        item { SmartWidget() }
    }
}

// RICH WIDGETS

@Composable
private fun ScheduleWidget(events: List<FamilyEvent>) {
    DashboardWidgetCard(title = "Schedule", icon = Icons.Default.CalendarToday) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (events.isEmpty()) {
                Text("No events today", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            events.take(3).forEach { event ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(4.dp, 32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(event.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(event.timeString, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChoresWidget(tasks: List<TaskItem>) {
    val completed = tasks.count { it.isCompleted }
    val total = tasks.size.coerceAtLeast(1)
    DashboardWidgetCard(title = "Chores", icon = Icons.Default.CheckCircle) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { completed.toFloat() / total },
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 8.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("${(completed * 100) / total}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text("$completed Done", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("${total - completed} Remaining", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Next: Feed Buddy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun MealsWidget(meals: List<MealPlanItem>) {
    val dayOfWeek = remember { SimpleDateFormat("EEEE", Locale.getDefault()).format(Date()) }
    val meal = meals.find { it.dayOfWeek.equals(dayOfWeek, ignoreCase = true) }
    
    DashboardWidgetCard(title = "Meals", icon = Icons.Default.Restaurant) {
        Text("Tonight's Dinner", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Text(meal?.recipeTitle ?: "Not planned yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(28.dp), shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                Box(contentAlignment = Alignment.Center) { Text(if (meal != null) "👨‍🍳" else "🍴", fontSize = 14.sp) }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(meal?.chefMemberName?.let { "Chef: $it" } ?: "Tap to assign", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun MoodWidget(members: List<FamilyMember>) {
    DashboardWidgetCard(title = "Mood Board", icon = Icons.Default.Favorite) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            members.take(4).forEach { m ->
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                        Box(contentAlignment = Alignment.Center) { Text(m.avatarEmoji, fontSize = 18.sp) }
                    }
                    Text("😊", fontSize = 12.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text("Family Mood: Excellent", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
    }
}

@Composable
private fun ShoppingWidget() {
    DashboardWidgetCard(title = "Shopping", icon = Icons.Default.ShoppingCart) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("🛒 5 items needed", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text("• Organic Milk", style = MaterialTheme.typography.labelSmall)
            Text("• Fresh Eggs", style = MaterialTheme.typography.labelSmall)
            Text("Recently added: Coffee", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FamilyProgressWidget() {
    DashboardWidgetCard(title = "Family Progress", icon = Icons.Default.Stars) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DashboardGoalRow("Chore Streak", 0.9f, Color(0xFFE85D4F))
            DashboardGoalRow("Reading", 0.6f, Color(0xFF3B82F6))
            DashboardGoalRow("Exercise", 0.4f, Color(0xFF10B981))
        }
    }
}

@Composable
private fun DashboardGoalRow(label: String, progress: Float, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(32.dp),
            strokeWidth = 4.dp,
            color = color
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("${(progress * 100).toInt()}% towards goal", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
        }
    }
}

@Composable
private fun AchievementWidget() {
    DashboardWidgetCard(title = "Achievements", icon = Icons.Default.EmojiEvents) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AchievementBadge("🏆", "10 Day Streak")
            AchievementBadge("🌟", "Hero")
            AchievementBadge("🍎", "Healthy")
        }
    }
}

@Composable
private fun AchievementBadge(emoji: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
            Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = 24.sp) }
        }
        Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp)
    }
}

@Composable
private fun WeatherWidget() {
    DashboardWidgetCard(title = "Weather", icon = Icons.Default.Cloud) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("☀️", fontSize = 32.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("27°C Sunny", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("High of 30°C", style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Perfect day for outdoor play!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SmartWidget() {
    DashboardWidgetCard(title = "Pet Reminder", icon = Icons.Default.Pets) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                Text("🐶")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Feed Buddy", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text("Last fed at 7:00 AM", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun DashboardWidgetCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
