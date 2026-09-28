package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TrackPointConverter
import com.example.export.GpxExporter
import com.example.export.SocialStoryExporter
import com.example.model.CyclingActivityEntity
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialExportBottomSheet(
    activity: CyclingActivityEntity,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val points = TrackPointConverter.fromJson(activity.trackPointsJson)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "EXPORT & SOCIAL SHARE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Share Your Ride",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Choose a 9:16 Instagram Story format or standard GPX workout file:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Option 1: Transparent PNG Overlay
            ExportOptionCard(
                title = "Transparent PNG Overlay (9:16)",
                subtitle = "Glowing route line + telemetry badges on transparent alpha. Perfect for overlaying on your ride photos in Instagram Stories.",
                icon = Icons.Default.Layers,
                iconTint = CyanAccent,
                testTag = "export_transparent_overlay",
                onClick = {
                    val success = SocialStoryExporter.exportAndShareStory(
                        context = context,
                        style = SocialStoryExporter.StoryStyle.TRANSPARENT_OVERLAY,
                        title = activity.title,
                        distanceKm = activity.distanceMeters / 1000.0,
                        durationSeconds = activity.durationSeconds,
                        avgSpeedKmh = activity.avgSpeedKmh,
                        maxSpeedKmh = activity.maxSpeedKmh,
                        elevationGainM = activity.elevationGainMeters,
                        calories = activity.caloriesBurned,
                        startTime = activity.startTime,
                        points = points
                    )
                    if (!success) {
                        Toast.makeText(context, "Could not share story overlay", Toast.LENGTH_SHORT).show()
                    }
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Option 2: Pre-designed Athletic Poster Graphic
            ExportOptionCard(
                title = "Athletic Story Poster (9:16)",
                subtitle = "Styled high-tech dark slate portrait graphic featuring route map box, full 6-metric telemetry grid, and branding.",
                icon = Icons.Default.Image,
                iconTint = EmeraldPrimary,
                testTag = "export_story_poster",
                onClick = {
                    val success = SocialStoryExporter.exportAndShareStory(
                        context = context,
                        style = SocialStoryExporter.StoryStyle.POSTER_GRAPHIC,
                        title = activity.title,
                        distanceKm = activity.distanceMeters / 1000.0,
                        durationSeconds = activity.durationSeconds,
                        avgSpeedKmh = activity.avgSpeedKmh,
                        maxSpeedKmh = activity.maxSpeedKmh,
                        elevationGainM = activity.elevationGainMeters,
                        calories = activity.caloriesBurned,
                        startTime = activity.startTime,
                        points = points
                    )
                    if (!success) {
                        Toast.makeText(context, "Could not generate story poster", Toast.LENGTH_SHORT).show()
                    }
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Option 3: GPX Workout Export
            ExportOptionCard(
                title = "Export GPX File (.gpx)",
                subtitle = "Generate standard GPX 1.1 XML file with GPS trackpoints, timestamps, and elevations. Import into Strava, Garmin, or Komoot.",
                icon = Icons.Default.Description,
                iconTint = Color(0xFFF59E0B),
                testTag = "export_gpx_file",
                onClick = {
                    val success = GpxExporter.exportAndShareGpx(
                        context = context,
                        title = activity.title,
                        startTime = activity.startTime,
                        points = points
                    )
                    if (!success) {
                        Toast.makeText(context, "Could not export GPX file", Toast.LENGTH_SHORT).show()
                    }
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun ExportOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconTint.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
