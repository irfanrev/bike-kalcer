package com.example.ui.components

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.HyperLime
import com.example.ui.theme.ObsidianNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StravaOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.glassmorphic
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CountdownRideOverlay(
    onCountdownComplete: () -> Unit,
    onCancel: () -> Unit,
    onLocationReady: (latitude: Double, longitude: Double) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var count by remember { mutableIntStateOf(3) }
    var gpsReady by remember { mutableStateOf(false) }

    BackHandler {
        onCancel()
    }

    // Active GPS Pre-warming: warms up the GPS chip and fetches high-accuracy fix before ride starts
    LaunchedEffect(Unit) {
        try {
            val fusedLocation = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()
            fusedLocation.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        gpsReady = true
                        onLocationReady(loc.latitude, loc.longitude)
                    }
                }
        } catch (e: Exception) {
            // Permission or mock fallback
        }
    }

    // 3-second animated countdown sequence
    LaunchedEffect(Unit) {
        delay(950)
        count = 2
        delay(950)
        count = 1
        delay(950)
        count = 0 // "GO!"
        delay(650)
        onCountdownComplete()
    }

    // Pulsing background ambient transition
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF50B0F17))
            .clickable(enabled = false) {}
            .testTag("countdown_ride_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Ambient radial background glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        HyperLime.copy(alpha = 0.22f),
                        CyberCyan.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    radius = size.width * 0.7f
                ),
                radius = size.width * 0.7f
            )
        }

        // Center Countdown Hub
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Main Animated Number Container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .scale(if (count == 0) 1.15f else pulseScale)
            ) {
                // Outer Glowing Ring Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(HyperLime, CyberCyan, StravaOrange, HyperLime)
                        ),
                        style = Stroke(width = 8f)
                    )
                }

                // Inner Frosted Hub
                Box(
                    modifier = Modifier
                        .size(176.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF131A29))
                        .border(2.dp, GlassBorderSubtle, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = count,
                        transitionSpec = {
                            (scaleIn(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                initialScale = 0.3f
                            ) + fadeIn(tween(200)))
                                .togetherWith(
                                    scaleOut(
                                        animationSpec = tween(150, easing = LinearEasing),
                                        targetScale = 1.4f
                                    ) + fadeOut(tween(150))
                                )
                        },
                        label = "countdownNumber"
                    ) { targetCount ->
                        if (targetCount > 0) {
                            Text(
                                text = "$targetCount",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 92.sp,
                                    lineHeight = 92.sp,
                                    fontWeight = FontWeight.Black
                                ),
                                color = if (targetCount == 1) HyperLime else PureWhite
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "RIDE!",
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontSize = 44.sp,
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = HyperLime
                                )
                                Icon(
                                    imageVector = Icons.Default.DirectionsBike,
                                    contentDescription = null,
                                    tint = HyperLime,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Real-Time GPS Precision Pre-Warming Telemetry Status
            Box(
                modifier = Modifier
                    .glassmorphic(
                        cornerRadius = 24.dp,
                        backgroundColor = Color(0xCC131A29),
                        borderColor = if (gpsReady) HyperLime.copy(alpha = 0.5f) else GlassBorderSubtle
                    )
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (count) {
                                    3 -> StravaOrange
                                    2 -> CyberCyan
                                    else -> HyperLime
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when (count) {
                            3 -> "WARMING GPS SENSORS..."
                            2 -> "LOCKING SATELLITE ACCURACY..."
                            1 -> if (gpsReady) "HIGH PRECISION GPS READY (±2m)" else "OPTIMIZING SENSORS..."
                            else -> "PEDAL HARD! TRACKING ACTIVE"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = PureWhite,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Hold on tight and stay aware of road traffic",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(42.dp))

            // Abort / Cancel Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x22FFFFFF))
                    .border(1.dp, GlassBorderSubtle, RoundedCornerShape(20.dp))
                    .clickable { onCancel() }
                    .padding(horizontal = 24.dp, vertical = 10.dp)
                    .testTag("countdown_cancel_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CANCEL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
