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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.TrackingScreen
import com.example.ui.theme.BikeRouteTheme
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.MainViewModel
import com.google.android.gms.location.LocationServices

enum class ScreenTab {
    PLANNER,
    TRACKING,
    HISTORY,
    DETAIL
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BikeRouteTheme {
                val context = LocalContext.current
                var currentTab by remember { mutableStateOf(ScreenTab.PLANNER) }
                val trackingState by viewModel.trackingState.collectAsState()

                // Request location and notification permissions at startup
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

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        // Show bottom navigation bar when not on full cockpit screen
                        if (currentTab != ScreenTab.TRACKING && currentTab != ScreenTab.DETAIL) {
                            Column {
                                // Live Ride floating mini bar if tracking in background
                                AnimatedVisibility(
                                    visible = trackingState.isTracking,
                                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                            .clickable { currentTab = ScreenTab.TRACKING }
                                            .testTag("ride_banner_return_to_cockpit"),
                                        shape = RoundedCornerShape(16.dp),
                                        color = EmeraldPrimary,
                                        shadowElevation = 6.dp
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .background(Color.White, CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "RIDE IN PROGRESS: ${trackingState.formattedTime} • %.2f km".format(trackingState.distanceKm),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = "Return",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }

                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 8.dp
                                ) {
                                    NavigationBarItem(
                                        selected = currentTab == ScreenTab.PLANNER,
                                        onClick = { currentTab = ScreenTab.PLANNER },
                                        icon = { Icon(Icons.Default.Map, contentDescription = "Planner") },
                                        label = { Text("Route Planner") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = EmeraldPrimary,
                                            indicatorColor = EmeraldPrimary.copy(alpha = 0.15f)
                                        ),
                                        modifier = Modifier.testTag("nav_item_planner")
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == ScreenTab.TRACKING,
                                        onClick = { currentTab = ScreenTab.TRACKING },
                                        icon = { Icon(Icons.Default.DirectionsBike, contentDescription = "Tracker") },
                                        label = { Text("Live Ride") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = EmeraldPrimary,
                                            indicatorColor = EmeraldPrimary.copy(alpha = 0.15f)
                                        ),
                                        modifier = Modifier.testTag("nav_item_tracker")
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == ScreenTab.HISTORY,
                                        onClick = { currentTab = ScreenTab.HISTORY },
                                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                                        label = { Text("Activities") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = EmeraldPrimary,
                                            indicatorColor = EmeraldPrimary.copy(alpha = 0.15f)
                                        ),
                                        modifier = Modifier.testTag("nav_item_history")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            ScreenTab.PLANNER -> HomeScreen(
                                viewModel = viewModel,
                                onNavigateToTracking = { currentTab = ScreenTab.TRACKING }
                            )
                            ScreenTab.TRACKING -> TrackingScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentTab = ScreenTab.PLANNER },
                                onRideFinished = { savedActivityId ->
                                    currentTab = ScreenTab.DETAIL
                                }
                            )
                            ScreenTab.HISTORY -> HistoryScreen(
                                viewModel = viewModel,
                                onActivityClick = { id ->
                                    viewModel.loadActivityDetail(id)
                                    currentTab = ScreenTab.DETAIL
                                }
                            )
                            ScreenTab.DETAIL -> DetailScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentTab = ScreenTab.HISTORY }
                            )
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
