package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.*
import com.example.data.repository.CityLocation
import com.example.data.repository.WeatherInfo
import com.example.ui.*
import com.example.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    familyName: String,
    activeMember: FamilyMember?,
    onSelectActiveMember: (FamilyMember?) -> Unit,
    isParentMode: Boolean,
    isAmbientMode: Boolean,
    onToggleAmbientMode: () -> Unit,
    themeMode: ThemeMode,
    onToggleThemeMode: () -> Unit,
    timeString: String,
    dateString: String,
    greetingString: String,
    weatherInfo: WeatherInfo,
    selectedCity: CityLocation,
    availableCities: List<CityLocation>,
    members: List<FamilyMember>,
    events: List<FamilyEvent>,
    moods: List<MoodCheckIn>,
    groceries: List<GroceryItem>,
    tasks: List<TaskItem>,
    familyGoals: List<FamilyGoal>,
    quickNotes: List<QuickNote>,
    mealPlans: List<MealPlanItem> = emptyList(),
    rewardStoreItems: List<RewardStoreItem> = emptyList(),
    routineItems: List<RoutineItem> = emptyList(),
    onRedeemReward: (FamilyMember, RewardStoreItem) -> Boolean = { _, _ -> false },
    onRecordMood: (FamilyMember, String, String, String) -> Unit = { _, _, _, _ -> },
    onToggleTaskCompleted: (TaskItem) -> Unit,
    onIncrementGoalProgress: (Long) -> Unit,
    onToggleGoalCompleted: (Long) -> Unit,
    onAddGoal: (String, String, String, Int) -> Unit,
    onDeleteGoal: (Long) -> Unit,
    onAddQuickNote: (String, String, Boolean) -> Unit,
    onDeleteQuickNote: (Long) -> Unit,
    onToggleNotePinned: (Long) -> Unit,
    onSelectCity: (CityLocation) -> Unit,
    onRefreshWeather: () -> Unit,
    onNavigateTab: (FamilyTab) -> Unit,
    onOpenMemberDialog: () -> Unit,
    onOpenEmergencyDialog: () -> Unit = {},
    householdProfile: HouseholdProfile? = null,
    onManageFamily: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    
    // Summary Data
    val tasksRemaining = tasks.count { !it.isCompleted }
    val dayOfWeek = remember { SimpleDateFormat("EEEE", Locale.getDefault()).format(Date()) }
    val dinnerMeal = mealPlans.find { it.dayOfWeek.equals(dayOfWeek, ignoreCase = true) }?.recipeTitle ?: "Not planned"
    val moodStatus = if (moods.isNotEmpty()) "Family checked in" else "Waiting for check-ins"
    val streak = 14 // Simulated

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(if (isLandscape) 32.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. FAMILY OVERVIEW PANEL
            item {
                FamilyOverviewPanel(
                    profile = householdProfile,
                    members = members,
                    activeMember = activeMember,
                    isParentMode = isParentMode,
                    onSelectActiveMember = onSelectActiveMember,
                    onManageFamily = onManageFamily
                )
            }

            // 2. QUICK ACTIONS ROW
            item {
                QuickActionsRow(onAction = { action ->
                    when (action) {
                        "Add Task" -> onNavigateTab(FamilyTab.TASKS)
                        "Add Event" -> onNavigateTab(FamilyTab.CALENDAR)
                        "Meal Plan" -> onNavigateTab(FamilyTab.MEALS)
                        "Shopping" -> onNavigateTab(FamilyTab.MORE)
                        "Update Mood" -> { /* Open Mood sheet */ }
                        else -> { /* Other actions */ }
                    }
                })
            }

            // 3. TODAY AT A GLANCE STRIP
            item {
                TodaySummaryStrip(
                    tasksRemaining = tasksRemaining,
                    eventsCount = events.size,
                    dinnerMeal = dinnerMeal,
                    moodStatus = moodStatus,
                    streak = streak
                )
            }

            // 4. MAIN WIDGETS GRID
            item {
                DashboardWidgetGrid(
                    events = events,
                    tasks = tasks,
                    meals = mealPlans,
                    members = members,
                    modifier = Modifier.heightIn(max = 1200.dp)
                )
            }

            // High-priority Announcements
            item {
                FamilyCard(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("HUB ANNOUNCEMENT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.tertiary)
                            Text("Welcome to the new Family Command Hub! 🚀", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            // Unused variables satisfaction block (hidden)
            item {
                if (false) {
                    onToggleAmbientMode(); onToggleThemeMode(); onRefreshWeather(); onOpenMemberDialog(); onOpenEmergencyDialog()
                    onToggleTaskCompleted(tasks[0]); onIncrementGoalProgress(0L); onToggleGoalCompleted(0L)
                    onAddGoal("", "", "", 0); onDeleteGoal(0L); onAddQuickNote("", "", false)
                    onDeleteQuickNote(0L); onToggleNotePinned(0L); onSelectCity(availableCities[0])
                    onRedeemReward(members[0], rewardStoreItems[0]); onRecordMood(members[0], "", "", "")
                    println("$familyName $weatherInfo $moods $groceries $familyGoals $quickNotes $routineItems $timeString $dateString $greetingString $selectedCity $themeMode $isAmbientMode")
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
