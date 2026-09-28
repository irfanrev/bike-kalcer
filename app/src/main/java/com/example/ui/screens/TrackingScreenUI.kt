package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CountdownRideOverlay
import com.example.ui.components.OsmMapView
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricCoral
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.HyperLime
import com.example.ui.theme.ObsidianNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StravaOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.glassmorphic
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TrackingScreenUI(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onRideFinished: (Long) -> Unit
) {
    val trackingState by viewModel.trackingState.collectAsState()
    var showFinishConfirmDialog by remember { mutableStateOf(false) }
    var showCountdown by remember { mutableStateOf(false) }

    // Pulsing animation for GPS Signal status dot
    val infiniteTransition = rememberInfiniteTransition(label = "gpsPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gpsPulse"
    )

    BackHandler {
        onNavigateBack()
    }

    val polylineCoords = remember(trackingState.trackPoints) {
        trackingState.trackPoints.map { Pair(it.latitude, it.longitude) }
    }
    val currentCoord = trackingState.currentPoint?.let { Pair(it.latitude, it.longitude) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianNavy)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                bottom = 24.dp
            )
    ) {
        // ==========================================
        // 1. TOP BAR: Minimize Pill + Floating GPS Status
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Minimize Pill
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .glassmorphic(cornerRadius = 21.dp, backgroundColor = Color(0xCC131A29))
                    .clickable { onNavigateBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Minimize",
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Floating GPS Signal Status Pill
            Box(
                modifier = Modifier
                    .glassmorphic(
                        cornerRadius = 20.dp,
                        backgroundColor = Color(0xCC131A29),
                        borderColor = GlassBorderSubtle
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (trackingState.isPaused) StravaOrange.copy(alpha = pulseAlpha)
                                else HyperLime.copy(alpha = pulseAlpha)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (trackingState.isPaused) "PAUSED" else "GPS STRONG",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = if (trackingState.isPaused) StravaOrange else HyperLime,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• LIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            // Empty spacer for symmetry
            Spacer(modifier = Modifier.size(42.dp))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================
        // 2. METRICS HUD (MASSIVE DISPLAY NUMBERS)
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .glassmorphic(
                    cornerRadius = 32.dp,
                    backgroundColor = Color(0xD9121826),
                    borderColor = GlassBorderSubtle,
                    elevation = 12.dp
                )
                .padding(20.dp)
        ) {
            Column {
                // Top Row: Speed (Hero) & Distance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // SPEED HERO
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SPEED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "%.1f".format(trackingState.currentSpeedKmh),
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Black,
                                color = HyperLime,
                                fontSize = 54.sp,
                                lineHeight = 56.sp
                            )
                            Text(
                                text = " km/h",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }

                    // DISTANCE HERO
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "DISTANCE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "%.2f".format(trackingState.distanceKm),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Black,
                                color = PureWhite,
                                fontSize = 42.sp,
                                lineHeight = 46.sp
                            )
                            Text(
                                text = " km",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Divider line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0x1AFFFFFF))
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Secondary 3-Column Grid: Elapsed Time, Avg Speed, Elevation Gain
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("TIME", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            text = trackingState.formattedTime,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = CyberCyan
                        )
                    }

                    Column {
                        Text("AVG SPEED", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            text = "%.1f km/h".format(trackingState.avgSpeedKmh),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = PureWhite
                        )
                    }

                    Column {
                        Text("ELEV GAIN", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            text = "+%.0f m".format(trackingState.elevationGainMeters),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = StravaOrange
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ==========================================
        // 3. CENTER MAP AREA (MINI LIVE MAP WITH ACTIVE PATH)
        // ==========================================
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .glassmorphic(
                    cornerRadius = 28.dp,
                    backgroundColor = Color(0xFF0F172A),
                    borderColor = GlassBorderSubtle
                )
        ) {
            OsmMapView(
                modifier = Modifier.fillMaxSize(),
                centerPoint = currentCoord,
                routePoints = polylineCoords,
                currentLocationPoint = currentCoord
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ==========================================
        // 4. CONTROL BAR (BIG CIRCULAR ACTIONS WITH SAFETY CONFIRM)
        // ==========================================
        if (!trackingState.isTracking) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Button(
                    onClick = { showCountdown = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(12.dp, RoundedCornerShape(28.dp), spotColor = HyperLime)
                        .testTag("cockpit_start_ride_button"),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HyperLime,
                        contentColor = ObsidianNavy
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Ride",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "START RIDE (3s GPS LOCK)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // PAUSE / RESUME CIRCULAR BUTTON
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(12.dp, CircleShape, spotColor = if (trackingState.isPaused) HyperLime else StravaOrange)
                            .clip(CircleShape)
                            .background(if (trackingState.isPaused) HyperLime else Color(0x33FF5722))
                            .border(2.dp, if (trackingState.isPaused) HyperLime else StravaOrange, CircleShape)
                            .clickable {
                                if (trackingState.isPaused) viewModel.resumeRide() else viewModel.pauseRide()
                            }
                            .testTag(if (trackingState.isPaused) "resume_ride_button" else "pause_ride_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (trackingState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (trackingState.isPaused) "Resume" else "Pause",
                            tint = if (trackingState.isPaused) ObsidianNavy else StravaOrange,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (trackingState.isPaused) "RESUME" else "PAUSE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = if (trackingState.isPaused) HyperLime else StravaOrange
                    )
                }

                // FINISH RIDE CIRCULAR BUTTON WITH SAFETY DIALOG
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(12.dp, CircleShape, spotColor = ElectricCoral)
                            .clip(CircleShape)
                            .background(ElectricCoral)
                            .clickable { showFinishConfirmDialog = true }
                            .testTag("finish_ride_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Finish Ride",
                            tint = PureWhite,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "FINISH",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = ElectricCoral
                    )
                }
            }
        }
    }

    // 3-Second GPS Pre-warming Countdown Overlay
    if (showCountdown) {
        CountdownRideOverlay(
            onCountdownComplete = {
                showCountdown = false
                viewModel.startRide()
            },
            onCancel = {
                showCountdown = false
            },
            onLocationReady = { lat, lon ->
                viewModel.updateCurrentLocation(lat, lon)
            }
        )
    }

    // Safety Confirmation Dialog for finishing ride
    if (showFinishConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showFinishConfirmDialog = false },
            containerColor = Color(0xFF131A29),
            title = {
                Text(
                    text = "Finish & Save Ride?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
            },
            text = {
                Text(
                    text = "Your ride of ${"%.2f".format(trackingState.distanceKm)} km will be finalized and stored in your career stats.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFinishConfirmDialog = false
                        viewModel.finishRide { savedId ->
                            onRideFinished(savedId)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HyperLime, contentColor = ObsidianNavy),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("YES, SAVE RIDE", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishConfirmDialog = false }) {
                    Text("KEEP RIDING", color = TextSecondary)
                }
            }
        )
    }
}
