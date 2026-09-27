package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class ConfettiParticle(
    val id: Int,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val size: Float,
    val color: Color,
    val rotationSpeed: Float,
    var currentRotation: Float = 0f,
    val isSquare: Boolean,
    val emoji: String? = null
)

@Composable
fun ConfettiOverlay(
    triggerSignal: Long,
    message: String = "🎉 Outstanding! Star Reward Earned! ⭐",
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    var particles by remember { mutableStateOf<List<ConfettiParticle>>(emptyList()) }

    val colors = listOf(
        Color(0xFFFF5252), Color(0xFFFF4081), Color(0xFFE040FB),
        Color(0xFF7C4DFF), Color(0xFF536DFE), Color(0xFF448AFF),
        Color(0xFF64FFDA), Color(0xFFB2FF59), Color(0xFFFFD700),
        Color(0xFFFF9800)
    )

    val emojis = listOf("⭐", "🎉", "✨", "🥳", "🎈", "🌟")

    LaunchedEffect(triggerSignal) {
        if (triggerSignal > 0) {
            // Generate 60 particles
            val newParticles = (0..60).map { id ->
                ConfettiParticle(
                    id = id,
                    x = Random.nextFloat() * 1000f,
                    y = -20f - Random.nextFloat() * 200f,
                    vx = (Random.nextFloat() - 0.5f) * 18f,
                    vy = Random.nextFloat() * 12f + 8f,
                    size = Random.nextFloat() * 18f + 10f,
                    color = colors[Random.nextInt(colors.size)],
                    rotationSpeed = (Random.nextFloat() - 0.5f) * 20f,
                    isSquare = Random.nextBoolean(),
                    emoji = if (Random.nextFloat() < 0.25f) emojis[Random.nextInt(emojis.size)] else null
                )
            }
            particles = newParticles
            isVisible = true

            // Particle physics animation loop
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < 2800) {
                particles = particles.map { p ->
                    p.copy(
                        x = p.x + p.vx,
                        y = p.y + p.vy,
                        vy = p.vy + 0.45f, // gravity
                        currentRotation = p.currentRotation + p.rotationSpeed
                    )
                }
                delay(16) // ~60fps
            }
            isVisible = false
        }
    }

    if (isVisible) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Particle Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                particles.forEach { p ->
                    val normX = (p.x % canvasWidth + canvasWidth) % canvasWidth
                    val normY = p.y

                    rotate(p.currentRotation, pivot = Offset(normX, normY)) {
                        if (p.isSquare) {
                            drawRect(
                                color = p.color,
                                topLeft = Offset(normX - p.size / 2, normY - p.size / 2),
                                size = Size(p.size, p.size * 0.7f)
                            )
                        } else {
                            drawCircle(
                                color = p.color,
                                radius = p.size / 2,
                                center = Offset(normX, normY)
                            )
                        }
                    }
                }
            }

            // Floating Banner Message
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
