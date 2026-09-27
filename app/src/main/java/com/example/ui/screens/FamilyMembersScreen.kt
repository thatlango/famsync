package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.FamilyMember
import com.example.ui.components.AvatarChip
import com.example.ui.components.FamilyCard
import com.example.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMembersScreen(
    members: List<FamilyMember>,
    onAddMember: (String, String, String, String, Int, Int, Int, String?) -> Unit,
    onUpdateMember: (FamilyMember) -> Unit,
    onDeleteMember: (FamilyMember) -> Unit,
    onBack: () -> Unit
) {
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Family Members") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddSheet = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Member")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionHeader(title = "Household Members", caption = "Manage permissions and profiles")
            }

            items(members.filter { !it.isArchived }) { member ->
                MemberProfileCard(
                    member = member,
                    onEdit = { /* Navigate to edit */ },
                    onArchive = { onUpdateMember(member.copy(isArchived = true)) }
                )
            }
            
            if (members.any { it.isArchived }) {
                item {
                    Text("Archived Members", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 16.dp))
                }
                items(members.filter { it.isArchived }) { member ->
                    MemberProfileCard(
                        member = member,
                        onEdit = { /* Restore */ },
                        onArchive = { onDeleteMember(member) },
                        isArchived = true
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false }
        ) {
            AddMemberSheetContent(
                onAdd = { n, r, e, c, bm, bd, by, p ->
                    onAddMember(n, r, e, c, bm, bd, by, p)
                    showAddSheet = false
                }
            )
        }
    }
}

@Composable
private fun MemberProfileCard(
    member: FamilyMember,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    isArchived: Boolean = false
) {
    FamilyCard(
        containerColor = if (isArchived) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarChip(
                name = "",
                avatarEmoji = member.avatarEmoji,
                size = 56,
                onClick = onEdit
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(member.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(member.role, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Text("${member.points} ⭐", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            IconButton(onClick = onArchive) {
                Icon(
                    if (isArchived) Icons.Default.DeleteForever else Icons.Default.Archive,
                    null,
                    tint = if (isArchived) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AddMemberSheetContent(
    onAdd: (String, String, String, String, Int, Int, Int, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Child") }
    var relationship by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("#E05A47") }
    var emoji by remember { mutableStateOf("🧒") }
    
    val roles = listOf("Parent", "Guardian", "Child", "Teenager", "Caregiver", "Guest")
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .navigationBarsPadding()
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text("Edit Household Member", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.weight(1f).verticalScroll(scrollState), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Nickname (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Text("Hub Role", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(roles) { r ->
                    FilterChip(
                        selected = role == r,
                        onClick = { role = r },
                        label = { Text(r) }
                    )
                }
            }

            OutlinedTextField(
                value = relationship,
                onValueChange = { relationship = it },
                label = { Text("Relationship to Family") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("e.g. Daughter, Uncle") }
            )

            OutlinedTextField(
                value = school,
                onValueChange = { school = it },
                label = { Text("School or Workplace") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            Text("Personal Theme Color", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("#E05A47", "#3B82F6", "#EC4899", "#D97706", "#10B981").forEach { hex ->
                    Surface(
                        modifier = Modifier.size(40.dp).clip(CircleShape).clickable { color = hex },
                        color = Color(android.graphics.Color.parseColor(hex)),
                        border = if (color == hex) BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) else null
                    ) {}
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onAdd(name, role, emoji, color, 0, 0, 0, null) },
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Save Member Profile", fontWeight = FontWeight.Bold)
        }
    }
}
