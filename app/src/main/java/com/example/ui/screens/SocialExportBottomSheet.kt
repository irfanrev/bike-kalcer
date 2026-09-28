package com.example.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TrackPointConverter
import com.example.export.GpxExporter
import com.example.export.SocialStoryExporter
import com.example.model.CyclingActivityEntity
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.HyperLime
import com.example.ui.theme.ObsidianNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StravaOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.glassmorphic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class ExportOptionTab(
    val title: String,
    val isImage: Boolean,
    val style: SocialStoryExporter.StoryStyle?
) {
    CYBER_NEON("Cyber Neon", true, SocialStoryExporter.StoryStyle.CYBER_NEON_POSTER),
    HUD_OVERLAY("HUD Overlay", true, SocialStoryExporter.StoryStyle.MINIMALIST_HUD_OVERLAY),
    PERFORMANCE("Performance", true, SocialStoryExporter.StoryStyle.ATHLETIC_PERFORMANCE_CARD),
    GPX_WORKOUT("GPX File", false, null)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialExportBottomSheet(
    activity: CyclingActivityEntity,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val points = remember(activity.trackPointsJson) {
        TrackPointConverter.fromJson(activity.trackPointsJson)
    }

    var selectedTab by remember { mutableStateOf(ExportOptionTab.CYBER_NEON) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGeneratingPreview by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }

    val distanceKm = activity.distanceMeters / 1000.0

    // Render Preview in background whenever selected tab changes
    LaunchedEffect(selectedTab) {
        if (selectedTab.isImage && selectedTab.style != null) {
            isGeneratingPreview = true
            val bmp = withContext(Dispatchers.Default) {
                SocialStoryExporter.generateBitmap(
                    style = selectedTab.style!!,
                    title = activity.title,
                    distanceKm = distanceKm,
                    durationSeconds = activity.durationSeconds,
                    avgSpeedKmh = activity.avgSpeedKmh,
                    maxSpeedKmh = activity.maxSpeedKmh,
                    elevationGainM = activity.elevationGainMeters,
                    calories = activity.caloriesBurned,
                    startTime = activity.startTime,
                    points = points
                )
            }
            previewBitmap = bmp
            isGeneratingPreview = false
        } else {
            previewBitmap = null
            isGeneratingPreview = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF111724),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Drag handle pill
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF263248))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sheet Title & Close Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "STORY EXPORT & PREVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = HyperLime,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Export & Share",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = PureWhite
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x22FFFFFF))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = PureWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // 1. TEMPLATE SELECTOR TABS
            // ==========================================
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(ExportOptionTab.values()) { _, tab ->
                    val isSelected = tab == selectedTab
                    val tabIcon = when (tab) {
                        ExportOptionTab.CYBER_NEON -> Icons.Default.AutoAwesome
                        ExportOptionTab.HUD_OVERLAY -> Icons.Default.Layers
                        ExportOptionTab.PERFORMANCE -> Icons.Default.Speed
                        ExportOptionTab.GPX_WORKOUT -> Icons.Default.Description
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) HyperLime else Color(0x1AFFFFFF))
                            .border(
                                1.dp,
                                if (isSelected) HyperLime else GlassBorderSubtle,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedTab = tab }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("tab_${tab.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = tabIcon,
                                contentDescription = null,
                                tint = if (isSelected) ObsidianNavy else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab.title.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) ObsidianNavy else PureWhite,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // 2. LIVE IMAGE / GPX PREVIEW AREA
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(390.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF0B0F17))
                    .border(1.5.dp, GlassBorderSubtle, RoundedCornerShape(24.dp))
                    .testTag("export_preview_container"),
                contentAlignment = Alignment.Center
            ) {
                if (selectedTab.isImage) {
                    if (isGeneratingPreview) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = HyperLime,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Rendering 9:16 Story Preview...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else if (previewBitmap != null) {
                        // Background backdrop if Transparent HUD is chosen so transparency is clear
                        if (selectedTab == ExportOptionTab.HUD_OVERLAY) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0xFF1E293B),
                                                Color(0xFF0F172A),
                                                Color(0xFF1E3A8A).copy(alpha = 0.5f)
                                            )
                                        )
                                    )
                            )
                        }

                        // Render the actual 1080x1920 generated bitmap
                        Image(
                            bitmap = previewBitmap!!.asImageBitmap(),
                            contentDescription = "9:16 Story Preview",
                            modifier = Modifier
                                .fillMaxSize()
                                .aspectRatio(9f / 16f, matchHeightConstraintsFirst = true)
                                .clip(RoundedCornerShape(22.dp)),
                            contentScale = ContentScale.Fit
                        )

                        // Preview overlay tag
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xCC000000))
                                .border(1.dp, GlassBorderSubtle, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (selectedTab == ExportOptionTab.HUD_OVERLAY) "TRANSPARENT PNG (9:16)" else "INSTAGRAM STORY (9:16)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = HyperLime,
                                fontSize = 9.sp
                            )
                        }
                    }
                } else {
                    // GPX Workout File Info Preview
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(HyperLime.copy(alpha = 0.15f))
                                .border(1.dp, HyperLime.copy(alpha = 0.35f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = HyperLime,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "GPX 1.1 Workout File",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${points.size} Trackpoints • Standard Lat/Lon & Elevation XML",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Compatibility Pills
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CompatPill("Strava")
                            CompatPill("Garmin")
                            CompatPill("Komoot")
                            CompatPill("Wahoo")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Explanation caption
            Text(
                text = when (selectedTab) {
                    ExportOptionTab.CYBER_NEON -> "Option 1: Bold athletic dark-mode poster with glowing route art & high-impact telemetry."
                    ExportOptionTab.HUD_OVERLAY -> "Option 2: Transparent PNG HUD card. Overlay directly on top of your riding photos in Instagram Stories."
                    ExportOptionTab.PERFORMANCE -> "Option 3: Clean performance card with route map snippet, split stats table, and elevation wave."
                    ExportOptionTab.GPX_WORKOUT -> "Option 4: Standard GPX file with timestamps, coordinates, and altitudes. Import into any cycling app."
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // 3. ACTION BUTTONS: DOWNLOAD & SHARE
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // DOWNLOAD BUTTON (ALL OPTIONS)
                OutlinedButton(
                    onClick = {
                        if (selectedTab.isImage && selectedTab.style != null) {
                            isDownloading = true
                            val success = SocialStoryExporter.saveStoryToGallery(
                                context = context,
                                style = selectedTab.style!!,
                                title = activity.title,
                                distanceKm = distanceKm,
                                durationSeconds = activity.durationSeconds,
                                avgSpeedKmh = activity.avgSpeedKmh,
                                maxSpeedKmh = activity.maxSpeedKmh,
                                elevationGainM = activity.elevationGainMeters,
                                calories = activity.caloriesBurned,
                                startTime = activity.startTime,
                                points = points
                            )
                            isDownloading = false
                            if (success) {
                                Toast.makeText(context, "Saved to Gallery / Photos 📸", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // GPX Download
                            val success = GpxExporter.saveGpxToDownloads(
                                context = context,
                                title = activity.title,
                                startTime = activity.startTime,
                                points = points
                            )
                            if (success) {
                                Toast.makeText(context, "Saved GPX to Downloads/BikeRoute 📁", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to save GPX file", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("download_action_button"),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, GlassBorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite)
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = PureWhite, strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.Download, contentDescription = "Download", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedTab.isImage) "DOWNLOAD" else "SAVE GPX",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // SHARE STORY / SHARE GPX (ALL OPTIONS)
                Button(
                    onClick = {
                        if (selectedTab.isImage && selectedTab.style != null) {
                            val success = SocialStoryExporter.exportAndShareStory(
                                context = context,
                                style = selectedTab.style!!,
                                title = activity.title,
                                distanceKm = distanceKm,
                                durationSeconds = activity.durationSeconds,
                                avgSpeedKmh = activity.avgSpeedKmh,
                                maxSpeedKmh = activity.maxSpeedKmh,
                                elevationGainM = activity.elevationGainMeters,
                                calories = activity.caloriesBurned,
                                startTime = activity.startTime,
                                points = points
                            )
                            if (!success) Toast.makeText(context, "Error sharing story", Toast.LENGTH_SHORT).show()
                        } else {
                            val success = GpxExporter.exportAndShareGpx(
                                context = context,
                                title = activity.title,
                                startTime = activity.startTime,
                                points = points
                            )
                            if (!success) Toast.makeText(context, "Error sharing GPX", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(52.dp)
                        .shadow(10.dp, RoundedCornerShape(24.dp), spotColor = HyperLime)
                        .testTag("share_action_button"),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HyperLime,
                        contentColor = ObsidianNavy
                    )
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (selectedTab.isImage) "SHARE STORY" else "SHARE GPX",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CompatPill(name: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x22FFFFFF))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = PureWhite
        )
    }
}
