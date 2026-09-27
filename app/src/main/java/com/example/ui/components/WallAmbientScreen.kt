package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.WeatherInfo
import java.util.Calendar

@Composable
fun WallAmbientScreen(
    familyName: String,
    timeString: String,
    dateString: String,
    weatherInfo: WeatherInfo,
    cityName: String,
    onExitAmbientMode: () -> Unit
) {
    // Determine auto night mode (9:00 PM to 6:30 AM)
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val currentMinute = remember { Calendar.getInstance().get(Calendar.MINUTE) }
    val isAutoNightTime = currentHour >= 21 || currentHour < 6 || (currentHour == 6 && currentMinute < 30)

    var isNightModeOverride by remember { mutableStateOf<Boolean?>(null) }
    val isNightMode = isNightModeOverride ?: isAutoNightTime

    var isPhotoFrameMode by remember { mutableStateOf(false) }
    var photoIndex by remember { mutableIntStateOf(0) }
    var showMotionGreeting by remember { mutableStateOf(false) }

    // Family Slideshow Cards
    val photoSlideshow = remember {
        listOf(
            "🏞️" to "Family Mountain Hike 2026",
            "🏖️" to "Summer Beach Day Memories",
            "🏕️" to "Weekend Camping & Bonfire",
            "🍰" to "Birthday Celebrations at Home"
        )
    }

    LaunchedEffect(isPhotoFrameMode) {
        if (isPhotoFrameMode) {
            while (true) {
                kotlinx.coroutines.delay(8000)
                photoIndex = (photoIndex + 1) % photoSlideshow.size
            }
        }
    }

    // Rotating inspirational family quotes
    val quotes = remember {
        listOf(
            "“Family is where life begins and love never ends.” ❤️",
            "“Together is our favorite place to be.” 🏡",
            "“Kindness in our home makes every day brighter.” ✨",
            "“Every day is a fresh start for new adventures!” 🚀",
            "“Small steps together build big happy memories.” 🌟"
        )
    }

    var quoteIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(12000)
            quoteIndex = (quoteIndex + 1) % quotes.size
        }
    }

    // Gentle floating pulsing animation for ambient background
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_pulse"
    )

    // Color theme variables based on Night Dimming
    val backgroundColors = if (isNightMode) {
        listOf(Color(0xFF1A0505), Color(0xFF2B0A0A), Color(0xFF100202)) // Warm low-red ultra-soft night dim gradient
    } else {
        listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF31103F)) // Deep indigo twilight
    }

    val clockTextColor = if (isNightMode) Color(0xFFFF8A65) else Color.White
    val subtitleTextColor = if (isNightMode) Color(0xFFFFCC80) else Color(0xFFE2E8F0)
    val glowColor = if (isNightMode) Color(0xFFD32F2F) else Color(0xFF818CF8)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = backgroundColors))
            .clickable {
                showMotionGreeting = true
            }
            .testTag("wall_ambient_screen")
    ) {
        // Subtle ambient glowing background aura
        Box(
            modifier = Modifier
                .size(500.dp)
                .scale(pulseScale)
                .alpha(alphaAnim * (if (isNightMode) 0.15f else 0.25f))
                .align(Alignment.Center)
                .background(glowColor, CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🏡", fontSize = 18.sp)
                        Text(
                            text = if (isNightMode) "🌙 Low-Red Night Mode (9 PM - 6:30 AM)" else "$familyName Wall Saver",
                            color = clockTextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Night Mode Toggle Button
                    IconButton(
                        onClick = { isNightModeOverride = !isNightMode },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.NightlightRound,
                            contentDescription = "Night Mode Toggle",
                            tint = if (isNightMode) Color(0xFFFF8A65) else Color.White
                        )
                    }

                    // Photo Frame Mode Toggle Button
                    IconButton(
                        onClick = { isPhotoFrameMode = !isPhotoFrameMode },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isPhotoFrameMode) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Photo Frame Mode",
                            tint = Color.White
                        )
                    }

                    // Simulated Proximity Motion Sensor Button
                    Button(
                        onClick = { showMotionGreeting = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Sensors, contentDescription = "Simulate Motion", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("👋 Motion Wake", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onExitAmbientMode,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Exit Ambient", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Exit", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Proximity Greeting Banner
            AnimatedVisibility(visible = showMotionGreeting) {
                Surface(
                    color = Color(0xFFFFB800),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .clickable { showMotionGreeting = false }
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "👋 Welcome back, $familyName! Tap anywhere to unlock full dashboard.",
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }

            // Center Display: Large Wall Clock & Photo / Quote Card
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 76.sp,
                    fontWeight = FontWeight.Black,
                    color = clockTextColor,
                    letterSpacing = 2.sp
                )

                Text(
                    text = dateString,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 22.sp,
                    color = subtitleTextColor,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (isPhotoFrameMode) {
                    val activePhoto = photoSlideshow[photoIndex]
                    Surface(
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(activePhoto.first, fontSize = 42.sp)
                            Column {
                                Text(
                                    text = "🖼️ Family Photo Frame",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFDE68A)
                                )
                                Text(
                                    text = activePhoto.second,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else {
                    // Quote Card
                    Surface(
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = quotes[quoteIndex],
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isNightMode) Color(0xFFFFCC80) else Color(0xFFFDE68A),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
                        )
                    }
                }
            }

            // Bottom Weather & Attract Callout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(weatherInfo.iconEmoji, fontSize = 36.sp)
                    Column {
                        Text(
                            text = "${weatherInfo.temperatureC.toInt()}°C (${weatherInfo.temperatureF}°F) • $cityName",
                            color = clockTextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${weatherInfo.condition} • High ${weatherInfo.highF}°F | Low ${weatherInfo.lowF}°F",
                            color = subtitleTextColor.copy(alpha = 0.85f),
                            fontSize = 14.sp
                        )
                    }
                }

                Surface(
                    color = Color(0xFFF59E0B).copy(alpha = 0.25f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "👋 Touch anywhere to exit Wall Saver Mode",
                        color = Color(0xFFFDE68A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
