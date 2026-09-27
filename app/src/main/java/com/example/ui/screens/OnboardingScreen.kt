package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
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
import com.example.ui.components.AvatarChip
import com.example.ui.components.FamilyCard
import com.example.data.repository.CityLocation
import com.example.ui.DraftEmergencyContact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    availableCities: List<CityLocation>,
    searchResults: List<CityLocation> = emptyList(),
    onSearchCities: (String) -> Unit = {},
    onDetectLocation: (Double, Double) -> Unit = { _, _ -> },
    selectedCity: CityLocation,
    onSelectCity: (CityLocation) -> Unit,
    onCompleteOnboarding: (CityLocation, String, List<DraftMember>, List<String>, String, List<DraftEmergencyContact>) -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var familyName by remember { mutableStateOf("") }
    var parentPin by remember { mutableStateOf("") }
    
    val draftMembers = remember {
        mutableStateListOf(
            DraftMember("Parent", "Parent", "👤", 1, 1, 1990)
        )
    }

    val priorities = remember { mutableStateListOf("Calendar", "Chores", "Meals", "Mood") }
    
    val draftContacts = remember {
        mutableStateListOf(
            DraftEmergencyContact("Emergency contact", "Family", "", "")
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
        bottomBar = {
            Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step > 0) {
                        TextButton(onClick = { step-- }) { Text("Back") }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    if (step < 4) {
                        Button(
                            onClick = { step++ },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                        }
                    } else {
                        Button(
                            onClick = { onCompleteOnboarding(selectedCity, familyName.trim(), draftMembers, priorities, parentPin, draftContacts) },
                            enabled = familyName.isNotBlank() && com.example.util.ParentPin.isValid(parentPin),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Launch Family Hub", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Stepper
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(5) { i ->
                    Box(
                        modifier = Modifier
                            .size(if (step == i) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (step == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            when (step) {
                0 -> StepHousehold(familyName, { familyName = it }, selectedCity.name)
                1 -> StepMembers(draftMembers)
                2 -> StepPriorities(priorities)
                3 -> StepSecurity(parentPin, { parentPin = it })
                4 -> StepReview(familyName, draftMembers.size, priorities.size)
            }
        }
    }
}

@Composable
private fun StepHousehold(name: String, onNameChange: (String) -> Unit, city: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🏠 Create Household", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Establish your family hub identity", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Family Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("📍 Location: $city", style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun StepMembers(members: List<DraftMember>) {
    Column {
        Text("👨‍👩‍👧‍👦 Family Members", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(members) { m ->
                FamilyCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarChip("", m.avatarEmoji, false, 40) {}
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(m.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(m.role, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            item {
                OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Add, null)
                    Text("Add Member")
                }
            }
        }
    }
}

@Composable
private fun StepPriorities(selected: MutableList<String>) {
    val options = listOf("Calendar", "Chores", "Meals", "Grocery", "Rewards", "Mood", "Routines")
    Column {
        Text("🎯 Priorities", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Enable the tools your family needs", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(24.dp))
        options.forEach { opt ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Checkbox(checked = selected.contains(opt), onCheckedChange = {
                    if (it) selected.add(opt) else selected.remove(opt)
                })
                Text(opt, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun StepSecurity(pin: String, onPinChange: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🔐 Parent Security", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Create a PIN for management actions", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = { value -> if (value.length <= 8 && value.all(Char::isDigit)) onPinChange(value) },
            label = { Text("Parent PIN (4–8 digits)") },
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp)
        )
        if (pin == "1234") Text("Choose a PIN other than 1234.", color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun StepReview(name: String, members: Int, tools: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🚀 Review Hub", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        FamilyCard {
            Text("Household: $name", fontWeight = FontWeight.Bold)
            Text("Members: $members")
            Text("Enabled Tools: $tools")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Hub will launch in Shared Display Mode", style = MaterialTheme.typography.labelSmall)
    }
}

data class DraftMember(val name: String, val role: String, val avatarEmoji: String, val birthdayMonth: Int, val birthdayDay: Int, val birthdayYear: Int, val photoUri: String? = null)
