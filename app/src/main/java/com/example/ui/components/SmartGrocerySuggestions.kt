package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.entities.FamilyMember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SmartStaple(
    val name: String,
    val category: String,
    val quantity: String,
    val reason: String,
    val emoji: String
)

@Composable
fun SmartGrocerySuggestions(
    members: List<FamilyMember>,
    onAddGrocery: (name: String, category: String, quantity: String, addedBy: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<SmartStaple>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    val kidsCount = members.count { it.role.equals("Kid", true) || it.role.equals("Teen", true) || it.role.equals("Pet", true) }
    val totalHousehold = members.size.coerceAtLeast(2)

    fun fetchGeminiSuggestions() {
        coroutineScope.launch {
            isLoading = true

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                suggestions = getFallbackStaples(members)
                isLoading = false
                return@launch
            }

            val prompt = """
                Generate a list of 6 smart grocery staples for a household of $totalHousehold people ($kidsCount kids/pets).
                Provide response in raw plain JSON array with objects having fields: "name", "category", "quantity", "reason", "emoji".
                Example: [{"name":"Whole Milk","category":"Dairy","quantity":"1 Gallon","reason":"Great for kids daily breakfast","emoji":"🥛"}]
            """.trimIndent()

            try {
                val parsed = withContext(Dispatchers.IO) {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .build()

                    val jsonBody = JSONObject().apply {
                        put("contents", JSONArray().apply {
                            put(JSONObject().apply {
                                put("parts", JSONArray().apply {
                                    put(JSONObject().apply {
                                        put("text", prompt)
                                    })
                                })
                            })
                        })
                    }

                    val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
                    val request = Request.Builder()
                        .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                        .post(requestBody)
                        .build()

                    val response = client.newCall(request).execute()
                    val responseStr = response.body?.string() ?: ""

                    val list = mutableListOf<SmartStaple>()
                    if (responseStr.isNotBlank()) {
                        val rootObj = JSONObject(responseStr)
                        val candidates = rootObj.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val firstCandidate = candidates.getJSONObject(0)
                            val content = firstCandidate.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                val text = parts.getJSONObject(0).optString("text", "")
                                val jsonStart = text.indexOf("[")
                                val jsonEnd = text.lastIndexOf("]")
                                if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                                    val jsonArray = JSONArray(text.substring(jsonStart, jsonEnd + 1))
                                    for (i in 0 until jsonArray.length()) {
                                        val obj = jsonArray.getJSONObject(i)
                                        list.add(
                                            SmartStaple(
                                                name = obj.optString("name", "Staple Item"),
                                                category = obj.optString("category", "Pantry"),
                                                quantity = obj.optString("quantity", "1"),
                                                reason = obj.optString("reason", "Household essential"),
                                                emoji = obj.optString("emoji", "🛒")
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    list
                }

                if (parsed.isNotEmpty()) {
                    suggestions = parsed
                } else {
                    suggestions = getFallbackStaples(members)
                }
            } catch (e: Exception) {
                suggestions = getFallbackStaples(members)
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(members) {
        if (suggestions.isEmpty()) {
            fetchGeminiSuggestions()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("smart_grocery_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Smart Gemini AI Staples Suggestions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                IconButton(
                    onClick = { fetchGeminiSuggestions() },
                    enabled = !isLoading,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Gemini Suggestions",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Text(
                text = "Tailored for household of $totalHousehold (${members.joinToString { it.name }})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Analyzing family size & generating smart staples...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (suggestions.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(suggestions) { staple ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.width(200.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(staple.emoji, fontSize = 24.sp)
                                    Surface(
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = staple.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = staple.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Qty: ${staple.quantity}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    text = staple.reason,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                                )

                                Button(
                                    onClick = {
                                        onAddGrocery(staple.name, staple.category, staple.quantity, "Gemini AI")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add to List", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getFallbackStaples(members: List<FamilyMember>): List<SmartStaple> {
    val hasPets = members.any { it.role.equals("Pet", true) }
    val hasKids = members.any { it.role.equals("Kid", true) || it.role.equals("Teen", true) }

    val staples = mutableListOf(
        SmartStaple("Whole Organic Milk", "Dairy & Eggs", "1 Gallon", "Daily staple for breakfast & smoothies", "🥛"),
        SmartStaple("Fresh Farm Eggs", "Dairy & Eggs", "1 Dozen", "High-protein breakfast essential", "🥚"),
        SmartStaple("Organic Bananas", "Produce", "1 Bunch", "Quick healthy snack for everyone", "🍌"),
        SmartStaple("Whole Wheat Bread", "Bakery", "1 Loaf", "Daily sandwiches and toast", "🍞")
    )

    if (hasKids) {
        staples.add(SmartStaple("Cheddar Cheese Sticks", "Snacks", "1 Pack", "Convenient kid-friendly school snack", "🧀"))
        staples.add(SmartStaple("100% Fruit Juice Boxes", "Drinks", "1 Pack (8 count)", "Great for school lunches & trips", "🧃"))
    } else {
        staples.add(SmartStaple("Greek Yogurt Cups", "Dairy & Eggs", "4 Pack", "Probiotic protein breakfast staple", "🥣"))
    }

    if (hasPets) {
        staples.add(SmartStaple("Crunchy Pet Treats", "Pet Care", "1 Bag", "Reward treats for family pets", "🐶"))
    }

    return staples
}
