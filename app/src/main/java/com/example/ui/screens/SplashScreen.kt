package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.HyperLime
import com.example.ui.theme.ObsidianNavy
import com.example.ui.theme.StravaOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashCompleted: () -> Unit
) {
    val scaleAnim = remember { Animatable(0.7f) }
    val alphaAnim = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    val progressAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, animationSpec = tween(600))
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        delay(1800)
        onSplashCompleted()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianNavy)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onSplashCompleted() }
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Center radial gradient atmospheric glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(size.width / 2f, size.height * 0.45f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        HyperLime.copy(alpha = 0.18f * glowPulse),
                        StravaOrange.copy(alpha = 0.10f * glowPulse),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = size.width * 0.75f
                ),
                radius = size.width * 0.75f,
                center = centerOffset
            )
        }

        // Center Brand Identity
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scaleAnim.value)
                .alpha(alphaAnim.value)
                .padding(horizontal = 24.dp)
        ) {
            // Glowing neon logo badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(110.dp)
            ) {
                // Outer Glow ring
                Box(
                    modifier = Modifier
                        .size((96 * (0.9f + glowPulse * 0.15f)).dp)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    HyperLime.copy(alpha = 0.4f * glowPulse),
                                    StravaOrange.copy(alpha = 0.3f * glowPulse)
                                )
                            ),
                            CircleShape
                        )
                )

                // Core pill/circle container
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF131A29)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBike,
                        contentDescription = "BikeRoute Logo",
                        tint = HyperLime,
                        modifier = Modifier.size(46.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Punchy Typography "BIKEROUTE"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "BIKE",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "ROUTE",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = HyperLime,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-headline
            Text(
                text = "ZERO-COST • CYCLIST ELEVATION GIS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = StravaOrange,
                letterSpacing = 2.5.sp
            )
        }

        // Minimalist Bottom Tag: "Ride. Track. Share." with pulsing loading bar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 54.dp)
                .alpha(alphaAnim.value)
        ) {
            Text(
                text = "Ride.  Track.  Share.",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // High-tech minimal loading indicator bar
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF1F293D))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressAnim)
                        .height(3.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(StravaOrange, HyperLime, CyberCyan)
                            )
                        )
                )
            }
        }
    }
}
