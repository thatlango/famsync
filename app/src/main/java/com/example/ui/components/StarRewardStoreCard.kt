package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Star
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
import com.example.ui.RewardStoreItem

@Composable
fun StarRewardStoreCard(
    members: List<FamilyMember>,
    rewardStoreItems: List<RewardStoreItem>,
    onRedeemReward: (FamilyMember, RewardStoreItem) -> Boolean,
    modifier: Modifier = Modifier
) {
    var selectedMember by remember(members) { mutableStateOf(members.firstOrNull { it.role.contains("Kid", ignoreCase = true) } ?: members.firstOrNull()) }
    var redemptionMessage by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("star_reward_store_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF8E1) // Warm gold/star light container
        )
    ) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFB800)
                    ) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            Icon(Icons.Default.Redeem, contentDescription = null, tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Star Reward Store 🎁",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF5D4037)
                        )
                        Text(
                            text = "Redeem earned stars for family privileges",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8D6E63)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Select Kid Profile Selector
            Text(
                text = "Redeeming For:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6D4C41)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                items(members) { member ->
                    val isSelected = member.id == selectedMember?.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedMember = member
                            redemptionMessage = null
                        },
                        label = {
                            Text("${member.avatarEmoji} ${member.name} (${member.points} ⭐)")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFB800),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Redemption Success Alert Banner
            AnimatedVisibility(visible = redemptionMessage != null) {
                redemptionMessage?.let { msg ->
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        }
                    }
                }
            }

            // Reward Store Items Horizontal Scroll
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(rewardStoreItems, key = { it.id }) { reward ->
                    val currentPoints = selectedMember?.points ?: 0
                    val canAfford = currentPoints >= reward.costStars

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.width(170.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(reward.emoji, fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = reward.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3E2723)
                            )
                            Text(
                                text = reward.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF795548),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Button(
                                onClick = {
                                    val targetMember = selectedMember
                                    if (targetMember != null) {
                                        val success = onRedeemReward(targetMember, reward)
                                        if (success) {
                                            redemptionMessage = "🎉 Privilege Unlocked for ${targetMember.name}! Show Mom/Dad to claim."
                                        } else {
                                            redemptionMessage = "❌ Needs ${reward.costStars - targetMember.points} more stars!"
                                        }
                                    }
                                },
                                enabled = canAfford,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (canAfford) Color(0xFFFFB800) else Color.LightGray
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${reward.costStars} Stars", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
