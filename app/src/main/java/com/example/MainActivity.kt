package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entities.*
import com.example.ui.*
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.FamilyHubTheme

data class NavItem(
    val tab: FamilyTab,
    val title: String,
    val icon: ImageVector
)

class MainActivity : ComponentActivity() {

    private val viewModel: FamilyViewModel by viewModels()

    override fun onStop() {
        viewModel.exitParentMode()
        super.onStop()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

            FamilyHubTheme(themeMode = themeMode) {
                val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
                val profile by viewModel.householdProfile.collectAsStateWithLifecycle()
                val weatherInfo by viewModel.weatherState.collectAsStateWithLifecycle()
                val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()

                val members by viewModel.members.collectAsStateWithLifecycle()
                val events by viewModel.events.collectAsStateWithLifecycle()
                val moods by viewModel.moods.collectAsStateWithLifecycle()
                val groceries by viewModel.groceries.collectAsStateWithLifecycle()
                val tasks by viewModel.tasks.collectAsStateWithLifecycle()
                val familyGoals by viewModel.familyGoals.collectAsStateWithLifecycle()
                val quickNotes by viewModel.quickNotes.collectAsStateWithLifecycle()
                val customEmergencyContacts by viewModel.customEmergencyContacts.collectAsStateWithLifecycle()
                val activeMember by viewModel.activeMember.collectAsStateWithLifecycle()
                val isAmbientMode by viewModel.isAmbientMode.collectAsStateWithLifecycle()
                val mealPlans by viewModel.mealPlans.collectAsStateWithLifecycle()
                val rewardStoreItems by viewModel.rewardStoreItems.collectAsStateWithLifecycle()
                val routineItems by viewModel.routineItems.collectAsStateWithLifecycle()

                val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()
                val houseMode by viewModel.houseMode.collectAsStateWithLifecycle()
                val isParentMode by viewModel.isParentMode.collectAsStateWithLifecycle()

                var showMembersDialog by remember { mutableStateOf(false) }
                var showEmergencyDialog by remember { mutableStateOf(false) }
                var showParentPinDialog by remember { mutableStateOf(false) }
                var showHouseModeMenu by remember { mutableStateOf(false) }
                var showCreateEventSheet by remember { mutableStateOf(false) }
                var showCreateTaskSheet by remember { mutableStateOf(false) }

                val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
                val isScrollingDown = scrollBehavior.state.contentOffset < -10f

                val configuration = LocalConfiguration.current
                val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

                if (!isOnboardingCompleted) {
                    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
                    OnboardingScreen(
                        availableCities = viewModel.availableCities,
                        searchResults = searchResults,
                        onSearchCities = { viewModel.searchCities(it) },
                        onDetectLocation = { lat, lon -> viewModel.detectLocation(lat, lon) },
                        selectedCity = selectedCity,
                        onSelectCity = { viewModel.selectCity(it) },
                        onCompleteOnboarding = { city, famName, draftMembers, priorities, pin, draftContacts ->
                            viewModel.completeOnboarding(city, famName, draftMembers, priorities, pin, draftContacts)
                        }
                    )
                } else if (isAmbientMode) {
                    AmbientDisplayScreen(
                        timeString = viewModel.timeString.collectAsStateWithLifecycle().value,
                        dateString = viewModel.dateString.collectAsStateWithLifecycle().value,
                        weatherTemp = "${weatherInfo.temperatureF}°",
                        weatherIcon = weatherInfo.iconEmoji,
                        onExit = { viewModel.toggleAmbientMode() }
                    )
                } else {
                    val navItems = listOf(
                        NavItem(FamilyTab.HOME, "Home", Icons.Default.Home),
                        NavItem(FamilyTab.TASKS, "Tasks", Icons.Default.CheckCircle),
                        NavItem(FamilyTab.CALENDAR, "Calendar", Icons.Default.Event),
                        NavItem(FamilyTab.MEALS, "Meals", Icons.Default.RestaurantMenu),
                        NavItem(FamilyTab.MORE, "More", Icons.Default.MoreHoriz)
                    )

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            ClockHeader(
                                profile = profile,
                                activeMember = activeMember,
                                timeString = viewModel.timeString.collectAsStateWithLifecycle().value,
                                dateString = viewModel.dateString.collectAsStateWithLifecycle().value,
                                greeting = viewModel.greetingString.collectAsStateWithLifecycle().value,
                                weather = weatherInfo,
                                nextEvent = events.firstOrNull(),
                                onParentModeClick = { showParentPinDialog = true },
                                onEmergencyClick = { showEmergencyDialog = true }
                            )
                        },
                        floatingActionButton = {
                            Box(modifier = Modifier.padding(bottom = 16.dp)) {
                                ExtendedFloatingActionButton(
                                    onClick = {
                                        if (currentTab == FamilyTab.CALENDAR && isParentMode) { showCreateEventSheet = true }
                                        else if (currentTab == FamilyTab.TASKS && isParentMode) { showCreateTaskSheet = true }
                                        else { showEmergencyDialog = true }
                                    },
                                    expanded = !isScrollingDown,
                                    icon = {
                                        Icon(
                                            if ((currentTab == FamilyTab.TASKS || currentTab == FamilyTab.CALENDAR) && isParentMode) Icons.Default.Add else Icons.Default.Warning,
                                            contentDescription = null
                                        )
                                    },
                                    text = {
                                        Text(
                                            if ((currentTab == FamilyTab.TASKS || currentTab == FamilyTab.CALENDAR) && isParentMode) {
                                                if (currentTab == FamilyTab.TASKS) "New Task" else "New Event"
                                            } else "Emergency"
                                        )
                                    },
                                    containerColor = if ((currentTab == FamilyTab.TASKS || currentTab == FamilyTab.CALENDAR) && isParentMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    contentColor = Color.White
                                )
                            }
                        },
                        bottomBar = {
                            if (!isLandscape) {
                                NavigationBar(
                                    modifier = Modifier.testTag("bottom_navigation_bar"),
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 8.dp
                                ) {
                                    navItems.forEach { item ->
                                        NavigationBarItem(
                                            selected = currentTab == item.tab,
                                            onClick = { viewModel.selectTab(item.tab) },
                                            icon = { Icon(item.icon, contentDescription = item.title) },
                                            label = { Text(item.title, maxLines = 1, style = MaterialTheme.typography.labelSmall) },
                                            alwaysShowLabel = true,
                                            modifier = Modifier.testTag("nav_tab_${item.tab.name.lowercase()}")
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                            if (isLandscape) {
                                NavigationRail(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    header = {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        FloatingActionButton(
                                            onClick = { /* Global Search or Add */ },
                                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
                                        ) {
                                            Icon(Icons.Default.Add, null)
                                        }
                                    },
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    navItems.forEach { item ->
                                        val isSelected = currentTab == item.tab
                                        NavigationRailItem(
                                            selected = isSelected,
                                            onClick = { viewModel.selectTab(item.tab) },
                                            icon = {
                                                BadgedBox(
                                                    badge = {
                                                        if (item.tab == FamilyTab.TASKS && tasks.count { !it.isCompleted } > 0) {
                                                            Badge { Text("${tasks.count { !it.isCompleted }}") }
                                                        }
                                                    }
                                                ) {
                                                    Icon(item.icon, contentDescription = item.title, modifier = Modifier.size(24.dp))
                                                }
                                            },
                                            label = { Text(item.title, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                            alwaysShowLabel = true,
                                            colors = NavigationRailItemDefaults.colors(
                                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            
                            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                when (currentTab) {
                                    FamilyTab.HOME -> {
                                        HomeScreen(
                                            familyName = profile?.name ?: "Family Hub",
                                            activeMember = activeMember,
                                            onSelectActiveMember = { viewModel.selectActiveMember(it) },
                                            isParentMode = isParentMode,
                                            isAmbientMode = isAmbientMode,
                                            onToggleAmbientMode = { viewModel.toggleAmbientMode() },
                                            themeMode = themeMode,
                                            onToggleThemeMode = { viewModel.toggleThemeMode() },
                                            timeString = viewModel.timeString.collectAsStateWithLifecycle().value,
                                            dateString = viewModel.dateString.collectAsStateWithLifecycle().value,
                                            greetingString = viewModel.greetingString.collectAsStateWithLifecycle().value,
                                            weatherInfo = weatherInfo,
                                            selectedCity = selectedCity,
                                            availableCities = viewModel.availableCities,
                                            members = members,
                                            events = events,
                                            moods = moods,
                                            groceries = groceries,
                                            tasks = tasks,
                                            familyGoals = familyGoals,
                                            quickNotes = quickNotes,
                                            mealPlans = mealPlans,
                                            rewardStoreItems = rewardStoreItems,
                                            routineItems = routineItems,
                                            onRedeemReward = { m, r -> viewModel.redeemRewardStoreItem(m, r) },
                                            onRecordMood = { m, e, l, n -> viewModel.recordMood(m, e, l, n) },
                                            onToggleTaskCompleted = { viewModel.toggleTaskCompleted(it) },
                                            onIncrementGoalProgress = { viewModel.incrementGoalProgress(it) },
                                            onToggleGoalCompleted = { viewModel.toggleGoalCompleted(it) },
                                            onAddGoal = { t, c, e, tr -> viewModel.addFamilyGoal(t, c, e, tr) },
                                            onDeleteGoal = { viewModel.deleteFamilyGoal(it) },
                                            onAddQuickNote = { t, a, u -> viewModel.addQuickNote(t, a, u) },
                                            onDeleteQuickNote = { viewModel.deleteQuickNote(it) },
                                            onToggleNotePinned = { viewModel.toggleNotePinned(it) },
                                            onSelectCity = { viewModel.selectCity(it) },
                                            onRefreshWeather = { viewModel.refreshWeather() },
                                            onNavigateTab = { viewModel.selectTab(it) },
                                            onOpenMemberDialog = { showMembersDialog = true },
                                            onOpenEmergencyDialog = { showEmergencyDialog = true },
                                            householdProfile = profile,
                                            onManageFamily = { /* PIN Dialog then open Parent Center? */ }
                                        )
                                    }
                                    FamilyTab.CALENDAR -> {
                                        ScheduleScreen(
                                            events = events,
                                            members = members,
                                            isParentMode = isParentMode,
                                            showAddSheet = showCreateEventSheet,
                                            onAddSheetDismiss = { showCreateEventSheet = false },
                                            onAddEvent = { t, d, tm, c, a, k, l -> viewModel.addEvent(t, d, tm, c, a, k, l) },
                                            onDeleteEvent = { viewModel.deleteEvent(it) }
                                        )
                                    }
                                    FamilyTab.TASKS -> {
                                        TasksScreen(
                                            tasks = tasks,
                                            members = members,
                                            activeMember = activeMember,
                                            isParentMode = isParentMode,
                                            showAddSheet = showCreateTaskSheet,
                                            onAddSheetDismiss = { showCreateTaskSheet = false },
                                            onSelectActiveMember = { viewModel.selectActiveMember(it) },
                                            rewardStoreItems = rewardStoreItems,
                                            routineItems = routineItems,
                                            onAddTask = { t, d, m, p, c -> viewModel.addTask(t, d, m, p, c) },
                                            onToggleTaskCompleted = { viewModel.toggleTaskCompleted(it) },
                                            onDeleteTask = { viewModel.deleteTask(it) },
                                            onRotateChores = { viewModel.rotateWeeklyChores() },
                                            onToggleTaskRotation = { viewModel.toggleTaskRotation(it) },
                                            onRedeemReward = { m, r -> viewModel.redeemRewardStoreItem(m, r) }
                                        )
                                    }
                                    FamilyTab.MEALS -> {
                                        MealPlannerScreen(
                                            mealPlans = mealPlans,
                                            onAddIngredientsToGrocery = { ing, chef -> viewModel.addMealIngredientsToGroceryList(ing, chef) },
                                            onToggleMealCooked = { viewModel.toggleMealCooked(it) },
                                            onAddOrUpdateMeal = { d, ty, t, e, ing, chef -> viewModel.addMealPlanItem(d, ty, t, e, ing, chef) },
                                            onNavigateToGrocery = { viewModel.selectTab(FamilyTab.MORE) }
                                        )
                                    }
                                    FamilyTab.MORE -> {
                                        MoreScreen(
                                            groceries = groceries,
                                            moods = moods,
                                            members = members,
                                            activeMember = activeMember,
                                            rewardStoreItems = rewardStoreItems,
                                            householdProfile = profile,
                                            isParentMode = isParentMode,
                                            themeMode = themeMode,
                                            onToggleThemeMode = { viewModel.toggleThemeMode() },
                                            onUpdateHouseholdProfile = { viewModel.updateHouseholdProfile(it) },
                                            onChangeParentPin = { current, next -> viewModel.changeParentPin(current, next) },
                                            onLockParent = { viewModel.exitParentMode() },
                                            onAddFamilyMember = { n, r, e, c, bm, bd, by, p -> viewModel.addFamilyMember(n, r, e, c, bm, bd, by, p) },
                                            onUpdateFamilyMember = { viewModel.updateFamilyMember(it) },
                                            onDeleteFamilyMember = { viewModel.deleteFamilyMember(it) },
                                            onRecordMood = { m, e, l, n -> viewModel.recordMood(m, e, l, n) },
                                            onDeleteMood = { m -> viewModel.deleteMood(m) },
                                            onAddGrocery = { n, c, q, a -> viewModel.addGrocery(n, c, q, a) },
                                            onToggleGroceryPurchased = { i -> viewModel.toggleGroceryPurchased(i) },
                                            onDeleteGrocery = { i -> viewModel.deleteGrocery(i) },
                                            onClearCompletedGroceries = { viewModel.clearCompletedGroceries() },
                                            onRedeemReward = { m, r -> viewModel.redeemRewardStoreItem(m, r) }
                                        )
                                    }
                                }
                            }

                            if (showMembersDialog) {
                                FamilyMembersDialog(
                                    members = members,
                                    onAddMember = { n, r, e, c, bm, bd, by, p -> viewModel.addFamilyMember(n, r, e, c, bm, bd, by, p) },
                                    onDeleteMember = { viewModel.deleteFamilyMember(it) },
                                    onDismiss = { showMembersDialog = false }
                                )
                            }

                            if (showEmergencyDialog) {
                                EmergencyContactDialog(
                                    members = members,
                                    customEmergencyContacts = customEmergencyContacts,
                                    onDismiss = { showEmergencyDialog = false }
                                )
                            }

                            if (showParentPinDialog) {
                                ParentPinDialog(
                                    onConfirm = { viewModel.enterParentMode(it) },
                                    onDismiss = { showParentPinDialog = false }
                                )
                            }

                            if (showHouseModeMenu) {
                                ModalBottomSheet(onDismissRequest = { showHouseModeMenu = false }) {
                                    Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("Switch Household Mode", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        HouseMode.entries.forEach { mode ->
                                            val isSelected = houseMode == mode
                                            Surface(
                                                onClick = { viewModel.setHouseMode(mode); showHouseModeMenu = false },
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Text(text = mode.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyLarge, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                                    if (isSelected) { Spacer(modifier = Modifier.weight(1f)); Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary) }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
