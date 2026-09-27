package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.FamilyMember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyContactDialog(
    members: List<FamilyMember>,
    customEmergencyContacts: List<com.example.ui.DraftEmergencyContact> = emptyList(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val familyContacts = remember(members, customEmergencyContacts) {
        val list = mutableListOf<com.example.ui.components.EmergencyContactData>()
        
        // Primary Services
        list.add(com.example.ui.components.EmergencyContactData("Emergency Services", "Police / Fire / Ambulance", "911", "🚨"))
        
        // Custom Contacts
        customEmergencyContacts.forEach { c ->
            list.add(com.example.ui.components.EmergencyContactData(c.name, c.relation, c.phone, "📞"))
        }
        
        // Family Members
        members.filter { it.role != "Pet" }.forEach { m ->
            list.add(com.example.ui.components.EmergencyContactData(m.name, m.role, "555-0199", m.avatarEmoji))
        }
        list
    }

    fun dialNumber(phone: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phone")
            }
            context.startActivity(intent)
        } catch (e: Exception) {}
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Emergency Contacts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("One-tap quick call", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                items(familyContacts) { contact ->
                    EmergencyContactCard(
                        name = contact.name,
                        relation = contact.relation,
                        phone = contact.phone,
                        avatarEmoji = contact.iconEmoji,
                        onCall = { dialNumber(contact.phone) },
                        onMessage = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("sms:${contact.phone}")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

data class EmergencyContactData(
    val name: String,
    val relation: String,
    val phone: String,
    val iconEmoji: String
)
