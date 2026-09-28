package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.screens.AppInfoScreen
import com.example.ui.screens.AppInfoType
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.HomeScreenUI
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TrackingScreenUI
import com.example.ui.theme.BikeRouteTheme
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
import com.google.android.gms.location.LocationServices

enum class ScreenTab {
    PLANNER,
    TRACKING,
    HISTORY,
    DETAIL,
    APP_INFO
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BikeRouteTheme {
                val context = LocalContext.current
                var showSplash by remember { mutableStateOf(true) }
                var currentTab by remember { mutableStateOf(ScreenTab.PLANNER) }
                var selectedInfoType by remember { mutableStateOf(AppInfoType.ABOUT_APP) }
                val trackingState by viewModel.trackingState.collectAsState()

                // Permission Requests
                val permissionsToRequest = remember {
                    val list = mutableListOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        list.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    list.toTypedArray()
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                    val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                    if (fineGranted || coarseGranted) {
                        fetchDeviceLocation()
                    }
                }

                LaunchedEffect(Unit) {
                    val hasFine = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!hasFine) {
                        permissionLauncher.launch(permissionsToRequest)
                    } else {
                        fetchDeviceLocation()
                    }
                }

                Crossfade(targetState = showSplash, label = "splashTransition") { isSplash ->
                    if (isSplash) {
                        SplashScreen(
                            onSplashCompleted = { showSplash = false }
                        )
                    } else {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            containerColor = ObsidianNavy,
                            contentWindowInsets = WindowInsets(0, 0, 0, 0),
                            bottomBar = {
                                if (currentTab != ScreenTab.TRACKING && currentTab != ScreenTab.DETAIL && currentTab != ScreenTab.APP_INFO) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .windowInsetsPadding(WindowInsets.navigationBars)
                                            .padding(bottom = 12.dp)
                                    ) {
                                        // Floating Ride In Progress Banner
                                        AnimatedVisibility(
                                            visible = trackingState.isTracking,
                                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 20.dp, vertical = 6.dp)
                                                    .glassmorphic(
                                                        cornerRadius = 24.dp,
                                                        backgroundColor = Color(0xF2161F30),
                                                        borderColor = StravaOrange.copy(alpha = 0.5f),
                                                        elevation = 12.dp
                                                    )
                                                    .clickable { currentTab = ScreenTab.TRACKING }
                                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                                    .testTag("ride_banner_return_to_cockpit")
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(10.dp)
                                                                .clip(CircleShape)
                                                                .background(StravaOrange)
                                                        )
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Text(
                                                            text = "RECORDING: ${trackingState.formattedTime} • %.2f km".format(trackingState.distanceKm),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Black,
                                                            color = PureWhite,
                                                            letterSpacing = 0.5.sp
                                                        )
                                                    }
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = "COCKPIT",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Black,
                                                            color = HyperLime
                                                        )
                                                        Icon(
                                                            imageVector = Icons.Default.ChevronRight,
                                                            contentDescription = "Return",
                                                            tint = HyperLime,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Floating Glassmorphic Pill Navigation Bar
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 24.dp)
                                                .glassmorphic(
                                                    cornerRadius = 32.dp,
                                                    backgroundColor = Color(0xEE121824),
                                                    borderColor = GlassBorderSubtle,
                                                    elevation = 16.dp
                                                )
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceAround,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                GenZNavItem(
                                                    selected = currentTab == ScreenTab.PLANNER,
                                                    icon = Icons.Default.Map,
                                                    label = "EXPLORE",
                                                    testTag = "nav_item_planner",
                                                    onClick = { currentTab = ScreenTab.PLANNER }
                                                )
                                                GenZNavItem(
                                                    selected = currentTab == ScreenTab.TRACKING,
                                                    icon = Icons.Default.DirectionsBike,
                                                    label = "RIDE",
                                                    testTag = "nav_item_tracker",
                                                    onClick = { currentTab = ScreenTab.TRACKING }
                                                )
                                                GenZNavItem(
                                                    selected = currentTab == ScreenTab.HISTORY,
                                                    icon = Icons.Default.History,
                                                    label = "STATS",
                                                    testTag = "nav_item_history",
                                                    onClick = { currentTab = ScreenTab.HISTORY }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (currentTab) {
                                    ScreenTab.PLANNER -> HomeScreenUI(
                                        viewModel = viewModel,
                                        onNavigateToTracking = { currentTab = ScreenTab.TRACKING }
                                    )
                                    ScreenTab.TRACKING -> TrackingScreenUI(
                                        viewModel = viewModel,
                                        onNavigateBack = { currentTab = ScreenTab.PLANNER },
                                        onRideFinished = { savedId ->
                                            currentTab = ScreenTab.DETAIL
                                        }
                                    )
                                    ScreenTab.HISTORY -> HistoryScreen(
                                        viewModel = viewModel,
                                        onActivityClick = { id ->
                                            viewModel.loadActivityDetail(id)
                                            currentTab = ScreenTab.DETAIL
                                        },
                                        onNavigateToInfo = { infoType ->
                                            selectedInfoType = infoType
                                            currentTab = ScreenTab.APP_INFO
                                        }
                                    )
                                    ScreenTab.DETAIL -> DetailScreen(
                                        viewModel = viewModel,
                                        onNavigateBack = { currentTab = ScreenTab.HISTORY }
                                    )
                                    ScreenTab.APP_INFO -> AppInfoScreen(
                                        type = selectedInfoType,
                                        onNavigateBack = { currentTab = ScreenTab.HISTORY }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchDeviceLocation() {
        try {
            val fusedLocation = LocationServices.getFusedLocationProviderClient(this)
            fusedLocation.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    viewModel.updateCurrentLocation(loc.latitude, loc.longitude)
                }
            }
        } catch (e: Exception) {
            // Ignore if unavailable
        }
    }
}

@Composable
private fun GenZNavItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) HyperLime else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) ObsidianNavy else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            if (selected) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = ObsidianNavy,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
