package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.entities.FamilyMember
import com.example.data.entities.GroceryItem
import com.example.data.entities.HouseholdProfile
import com.example.data.entities.MoodCheckIn
import com.example.ui.RewardStoreItem
import com.example.ui.ThemeMode
import com.example.ui.components.FamilyCard
import com.example.ui.components.SectionHeader

@Composable
fun MoreScreen(
    groceries: List<GroceryItem>,
    moods: List<MoodCheckIn>,
    members: List<FamilyMember>,
    activeMember: FamilyMember?,
    rewardStoreItems: List<RewardStoreItem>,
    householdProfile: HouseholdProfile?,
    isParentMode: Boolean,
    themeMode: ThemeMode,
    onToggleThemeMode: () -> Unit,
    onUpdateHouseholdProfile: (HouseholdProfile) -> Unit,
    onAddFamilyMember: (String, String, String, String, Int, Int, Int, String?) -> Unit,
    onUpdateFamilyMember: (FamilyMember) -> Unit,
    onDeleteFamilyMember: (FamilyMember) -> Unit,
    onRecordMood: (FamilyMember, String, String, String) -> Unit,
    onDeleteMood: (MoodCheckIn) -> Unit,
    onAddGrocery: (String, String, String, String) -> Unit,
    onToggleGroceryPurchased: (GroceryItem) -> Unit,
    onDeleteGrocery: (GroceryItem) -> Unit,
    onClearCompletedGroceries: () -> Unit,
    onRedeemReward: (FamilyMember, RewardStoreItem) -> Boolean
) {
    var activeSubScreen by remember { mutableStateOf<String?>(null) }

    if (activeSubScreen != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (activeSubScreen) {
                "Grocery" -> {
                    GroceryScreen(
                        groceries = groceries,
                        members = members,
                        onAddGrocery = onAddGrocery,
                        onTogglePurchased = onToggleGroceryPurchased,
                        onDeleteGrocery = onDeleteGrocery,
                        onClearCompleted = onClearCompletedGroceries
                    )
                }
                "Mood" -> {
                    MoodScreen(
                        members = members,
                        moods = moods,
                        activeMember = activeMember,
                        isParentMode = isParentMode,
                        onRecordMood = onRecordMood,
                        onDeleteMood = onDeleteMood
                    )
                }
                "Household" -> {
                    HouseholdProfileScreen(
                        profile = householdProfile,
                        onUpdateProfile = onUpdateHouseholdProfile,
                        onBack = { activeSubScreen = null }
                    )
                }
                "Members" -> {
                    FamilyMembersScreen(
                        members = members,
                        onAddMember = onAddFamilyMember,
                        onUpdateMember = onUpdateFamilyMember,
                        onDeleteMember = onDeleteFamilyMember,
                        onBack = { activeSubScreen = null }
                    )
                }
                "Display" -> {
                    DisplaySettingsScreen(
                        onBack = { activeSubScreen = null }
                    )
                }
                "SmartHome" -> {
                    SmartHomeDashboard()
                }
                "ParentCenter" -> {
                    ParentCenterScreen(
                        profile = householdProfile,
                        onUpdateProfile = onUpdateHouseholdProfile,
                        onBack = { activeSubScreen = null }
                    )
                }
            }
            
            // Back button for sub-screens (Except Household/Members/Display/ParentCenter which have their own top bars)
            if (activeSubScreen != "Household" && activeSubScreen != "Members" && activeSubScreen != "Display" && activeSubScreen != "ParentCenter") {
                SmallFloatingActionButton(
                    onClick = { activeSubScreen = null },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(title = "Family Hub Tools", caption = if (isParentMode) "Parent Management Mode" else "Family Display Mode")
        }

        if (isParentMode) {
            item {
                FamilyCard(onClick = { activeSubScreen = "Household" }) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HomeWork, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Household Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(householdProfile?.name ?: "Family Identity", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            item {
                FamilyCard(onClick = { activeSubScreen = "Members" }) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Family Members", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${members.size} members connected", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            item {
                FamilyCard(onClick = { activeSubScreen = "Display" }) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TabletAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Display Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Hub optimization & night mode", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            item {
                FamilyCard(onClick = { activeSubScreen = "SmartHome" }) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HomeMax, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Smart Home", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Control lights, plugs and cameras", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            item {
                FamilyCard(onClick = { activeSubScreen = "ParentCenter" }) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Parent Center", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Security, backup and management", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        item {
            FamilyCard(onClick = { 
                // In a real app, this would open a dialog or sub-screen
                // For now, we'll just demonstrate it's linked
            }) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Reward Store", style = MaterialTheme.typography.titleMedium)
                        Text("${rewardStoreItems.size} rewards available", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        item {
            FamilyCard(onClick = onToggleThemeMode) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = when (themeMode) {
                            ThemeMode.LIGHT -> Icons.Default.LightMode
                            ThemeMode.DARK -> Icons.Default.DarkMode
                            ThemeMode.AUTO -> Icons.Default.BrightnessAuto
                        },
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Settings & Appearance", style = MaterialTheme.typography.titleMedium)
                        Text("Current Theme: ${themeMode.name}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
