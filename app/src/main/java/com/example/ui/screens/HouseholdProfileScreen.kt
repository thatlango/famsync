package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.HouseholdProfile
import com.example.ui.components.FamilyCard
import com.example.ui.components.SectionHeader
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdProfileScreen(
    profile: HouseholdProfile?,
    onUpdateProfile: (HouseholdProfile) -> Unit,
    onBack: () -> Unit
) {
    var name by remember(profile) { mutableStateOf(profile?.name ?: "") }
    var motto by remember(profile) { mutableStateOf(profile?.motto ?: "") }
    var location by remember(profile) { mutableStateOf(profile?.homeLocation ?: "") }
    var nickname by remember(profile) { mutableStateOf(profile?.nickname ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Household Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Family Photo Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (profile?.photoUri != null) {
                            // In a real app, use AsyncImage
                            Text("📸", fontSize = 48.sp)
                        } else {
                            Text("👨‍👩‍👧‍👦", fontSize = 48.sp)
                        }
                        
                        IconButton(
                            onClick = { /* Image picker */ },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Family Photo", style = MaterialTheme.typography.labelMedium)
            }

            FamilyCard {
                SectionHeader(title = "General Info")
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Household Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Family Nickname") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = motto,
                    onValueChange = { motto = it },
                    label = { Text("Family Motto") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Growing Together...") }
                )
            }

            FamilyCard {
                SectionHeader(title = "Location & Setup")
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Home Location") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.LocationOn, null) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                profile?.let {
                    val date = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(it.createdAtMillis))
                    Text("Created: $date", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Button(
                onClick = {
                    onUpdateProfile(
                        profile?.copy(name = name, motto = motto, homeLocation = location, nickname = nickname)
                            ?: HouseholdProfile(name = name, motto = motto, homeLocation = location, nickname = nickname)
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}
