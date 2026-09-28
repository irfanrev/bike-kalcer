package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TrackPointConverter
import com.example.ui.components.ElevationChart
import com.example.ui.components.OsmMapView
import com.example.ui.components.StatCard
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DetailScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val activity by viewModel.savedActivityDetail.collectAsState()
    var showExportSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    BackHandler {
        onNavigateBack()
    }

    if (activity == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianNavy),
            contentAlignment = Alignment.Center
        ) {
            Text("Activity not found", style = MaterialTheme.typography.titleMedium, color = PureWhite)
        }
        return
    }

    val currentActivity = activity!!
    val points = remember(currentActivity.trackPointsJson) {
        TrackPointConverter.fromJson(currentActivity.trackPointsJson)
    }
    val polylineCoords = remember(points) {
        points.map { Pair(it.latitude, it.longitude) }
    }
    val elevationProfile = remember(points) {
        points.map { it.altitude }
    }

    val distanceKm = currentActivity.distanceMeters / 1000.0
    val hours = currentActivity.durationSeconds / 3600
    val minutes = (currentActivity.durationSeconds % 3600) / 60
    val seconds = currentActivity.durationSeconds % 60
    val durationFormatted = if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)

    val dateFormatted = SimpleDateFormat("EEEE, MMMM d, yyyy • h:mm a", Locale.US).format(Date(currentActivity.startTime))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianNavy)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp
            )
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .glassmorphic(cornerRadius = 21.dp, backgroundColor = Color(0xCC131A29))
                    .clickable { onNavigateBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "RIDE BREAKDOWN",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = HyperLime,
                letterSpacing = 1.5.sp
            )

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .glassmorphic(cornerRadius = 21.dp, backgroundColor = Color(0xCC131A29))
                    .clickable { showDeleteDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = ElectricCoral,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Scrollable Body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = currentActivity.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = PureWhite
            )
            Text(
                text = dateFormatted,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Map View Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .glassmorphic(
                        cornerRadius = 28.dp,
                        backgroundColor = Color(0xFF0F172A),
                        borderColor = GlassBorderSubtle
                    )
            ) {
                OsmMapView(
                    modifier = Modifier.fillMaxSize(),
                    centerPoint = polylineCoords.firstOrNull(),
                    routePoints = polylineCoords
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 6-Metric Telemetry Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Distance",
                    value = "%.2f".format(distanceKm),
                    unit = "km",
                    valueColor = HyperLime,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Time",
                    value = durationFormatted,
                    unit = "",
                    valueColor = CyberCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Avg Speed",
                    value = "%.1f".format(currentActivity.avgSpeedKmh),
                    unit = "km/h",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Max Speed",
                    value = "%.1f".format(currentActivity.maxSpeedKmh),
                    unit = "km/h",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Elevation",
                    value = "+%.0f".format(currentActivity.elevationGainMeters),
                    unit = "m",
                    iconTint = StravaOrange,
                    valueColor = StravaOrange,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Calories",
                    value = "${currentActivity.caloriesBurned}",
                    unit = "kcal",
                    iconTint = ElectricCoral,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Elevation Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(
                        cornerRadius = 24.dp,
                        backgroundColor = Color(0xCC131A29),
                        borderColor = GlassBorderSubtle
                    )
                    .padding(18.dp)
            ) {
                ElevationChart(
                    elevationProfile = elevationProfile,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Large Pill Button: Share 9:16 Story / GPX
            Button(
                onClick = { showExportSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(12.dp, RoundedCornerShape(28.dp), spotColor = HyperLime)
                    .testTag("share_export_button"),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HyperLime,
                    contentColor = ObsidianNavy
                )
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SHARE STORY OR EXPORT GPX",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    if (showExportSheet) {
        SocialExportBottomSheet(
            activity = currentActivity,
            onDismiss = { showExportSheet = false }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF131A29),
            title = {
                Text(
                    text = "Delete Activity?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
            },
            text = {
                Text(
                    text = "This will permanently remove this ride and telemetry from your local database.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteActivity(currentActivity.id)
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCoral),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("DELETE", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }
}
