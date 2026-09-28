package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onActivityClick: (Long) -> Unit,
    onNavigateToInfo: (AppInfoType) -> Unit = {}
) {
    val activities by viewModel.activities.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    val totalDistanceKm = activities.sumOf { it.distanceMeters } / 1000.0
    val totalElevClimbed = activities.sumOf { it.elevationGainMeters }
    val totalTimeSeconds = activities.sumOf { it.durationSeconds }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianNavy)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                start = 16.dp,
                end = 16.dp
            )
    ) {
        // ==========================================
        // APP BAR: TITLE ON LEFT, ELLIPSIS BUTTON ON RIGHT
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CAREER & LOGS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = HyperLime,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Your Rides",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = PureWhite
                )
            }

            // Ellipsis Menu Button
            Box {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .glassmorphic(cornerRadius = 21.dp, backgroundColor = Color(0xCC131A29))
                        .clickable { showMenu = true }
                        .testTag("history_ellipsis_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Dropdown Menu with 3 Options
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier
                        .background(Color(0xFF131A29))
                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(16.dp))
                        .testTag("history_dropdown_menu")
                ) {
                    // Option 1: About This App
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = HyperLime,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "About This App",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            }
                        },
                        onClick = {
                            showMenu = false
                            onNavigateToInfo(AppInfoType.ABOUT_APP)
                        },
                        modifier = Modifier.testTag("menu_about_app")
                    )

                    // Option 2: Privacy Policy & Terms
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Privacy Policy & TnC",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            }
                        },
                        onClick = {
                            showMenu = false
                            onNavigateToInfo(AppInfoType.PRIVACY_POLICY)
                        },
                        modifier = Modifier.testTag("menu_privacy_policy")
                    )

                    // Option 3: App Version
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Numbers,
                                    contentDescription = null,
                                    tint = StravaOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "App Version",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            }
                        },
                        onClick = {
                            showMenu = false
                            onNavigateToInfo(AppInfoType.APP_VERSION)
                        },
                        modifier = Modifier.testTag("menu_app_version")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // High-Energy Athletic Hero Career Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(
                    cornerRadius = 28.dp,
                    backgroundColor = Color(0xD9121826),
                    borderColor = GlassBorderSubtle,
                    elevation = 10.dp
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIFETIME TELEMETRY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(HyperLime.copy(alpha = 0.18f))
                            .border(1.dp, HyperLime.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${activities.size} SESSIONS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = HyperLime
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("DISTANCE", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            text = "%.1f km".format(totalDistanceKm),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = PureWhite
                        )
                    }

                    Column {
                        Text("CLIMBED", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            text = "+%.0f m".format(totalElevClimbed),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = StravaOrange
                        )
                    }

                    Column {
                        Text("TIME", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        val hours = totalTimeSeconds / 3600
                        val mins = (totalTimeSeconds % 3600) / 60
                        Text(
                            text = "${hours}h ${mins}m",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = CyberCyan
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (activities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF131A29))
                            .border(1.dp, GlassBorderSubtle, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBike,
                            contentDescription = null,
                            tint = HyperLime,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No rides logged yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                    Text(
                        text = "Start tracking in the planner to build your career stats!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(activities, key = { it.id }) { activity ->
                    ActivityListItem(
                        activity = activity,
                        onClick = { onActivityClick(activity.id) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun ActivityListItem(
    activity: CyclingActivityEntity,
    onClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.US).format(Date(activity.startTime))
    val distanceKm = activity.distanceMeters / 1000.0

    val hours = activity.durationSeconds / 3600
    val minutes = (activity.durationSeconds % 3600) / 60
    val seconds = activity.durationSeconds % 60
    val durationFormatted = if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphic(
                cornerRadius = 20.dp,
                backgroundColor = Color(0xCC131A29),
                borderColor = GlassBorderSubtle,
                elevation = 4.dp
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
            .testTag("activity_item_${activity.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(HyperLime.copy(alpha = 0.15f))
                    .border(1.dp, HyperLime.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBike,
                    contentDescription = null,
                    tint = HyperLime,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "%.2f km".format(distanceKm),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = HyperLime
                    )
                    Text(
                        text = durationFormatted,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CyberCyan
                    )
                    Text(
                        text = "%.1f km/h".format(activity.avgSpeedKmh),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "+%.0fm".format(activity.elevationGainMeters),
                        style = MaterialTheme.typography.labelMedium,
                        color = StravaOrange
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Details",
                tint = TextSecondary
            )
        }
    }
}
