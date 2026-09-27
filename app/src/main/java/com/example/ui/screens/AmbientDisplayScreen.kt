package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AmbientDisplayScreen(
    timeString: String,
    dateString: String,
    weatherTemp: String,
    weatherIcon: String,
    onExit: () -> Unit
) {
    var mode by remember { mutableStateOf("Clock") } // "Clock", "Photo"
    
    // Simple burn-in protection: slightly shift content every minute
    var xOffset by remember { mutableFloatStateOf(0f) }
    var yOffset by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(timeString) {
        xOffset = (-10..10).random().toFloat()
        yOffset = (-10..10).random().toFloat()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { onExit() },
        contentAlignment = Alignment.Center
    ) {
        // Gradient backdrop for photo mode (simulated)
        if (mode == "Photo") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.DarkGray, Color.Black)
                        )
                    )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(xOffset.dp, yOffset.dp)
        ) {
            Text(
                text = timeString.split(" ")[0], // Large HH:MM
                style = MaterialTheme.typography.displayLarge,
                fontSize = 120.sp,
                fontWeight = FontWeight.Black,
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                text = dateString,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(weatherIcon, fontSize = 40.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = weatherTemp,
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Dashboard Hint
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            color = Color.White.copy(alpha = 0.1f),
            shape = CircleShape
        ) {
            Text(
                text = "Tap to unlock hub",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                color = Color.White.copy(alpha = 0.4f),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
