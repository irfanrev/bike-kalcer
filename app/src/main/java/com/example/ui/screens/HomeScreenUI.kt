package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CountdownRideOverlay
import com.example.ui.components.ElevationChart
import com.example.ui.components.OsmMapView
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassFrostWhite
import com.example.ui.theme.GlassSurfaceDark
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.HyperLime
import com.example.ui.theme.ObsidianNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StravaOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.glassmorphic
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenUI(
    viewModel: MainViewModel,
    onNavigateToTracking: () -> Unit
) {
    val currentLocation by viewModel.currentLocation.collectAsState()
    val destinationLocation by viewModel.destinationLocation.collectAsState()
    val destinationAddress by viewModel.destinationAddress.collectAsState()
    val routePlan by viewModel.routePlan.collectAsState()
    val selectedIndex by viewModel.selectedRouteIndex.collectAsState()
    val isRoutingLoading by viewModel.isRoutingLoading.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    val selectedRoute = routePlan?.routes?.getOrNull(selectedIndex)

    // Bottom sheet state for route preview & elevation
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showFullSheet by remember { mutableStateOf(false) }
    var showCountdown by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianNavy)
    ) {
        // Edge-to-Edge Immersive Map View
        OsmMapView(
            modifier = Modifier.fillMaxSize(),
            centerPoint = currentLocation,
            routePoints = selectedRoute?.coordinates ?: emptyList(),
            destinationPoint = destinationLocation,
            currentLocationPoint = currentLocation,
            onMapClick = { lat, lon ->
                viewModel.setDestination(lat, lon)
            }
        )

        // Top Floating Glassmorphic Search Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                    start = 16.dp,
                    end = 16.dp
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(
                        cornerRadius = 28.dp,
                        backgroundColor = Color(0xE6101626),
                        borderColor = GlassBorderSubtle
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Location pin icon pill
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(StravaOrange.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Destination Pin",
                                tint = StravaOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Text Field Input
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = {
                                Text(
                                    "Where are we riding today?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_destination_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = PureWhite
                            ),
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.clearDestination() }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = TextSecondary
                                        )
                                    }
                                } else if (isSearching) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = HyperLime
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        // Athletic Profile Avatar Pill
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(HyperLime, CyberCyan)
                                    )
                                )
                                .padding(1.5.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF131A29)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = PureWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Autocomplete Search Results
                    AnimatedVisibility(
                        visible = searchResults.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(top = 8.dp)
                        ) {
                            items(searchResults) { place ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.selectSearchResult(place) }
                                        .padding(horizontal = 8.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NearMe,
                                        contentDescription = null,
                                        tint = CyberCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = place.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = PureWhite
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Routing loading pill
            if (isRoutingLoading) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .glassmorphic(
                            cornerRadius = 20.dp,
                            backgroundColor = Color(0xEE131A29),
                            borderColor = GlassBorderSubtle
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = HyperLime
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Analyzing Topography & Grade...",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    }
                }
            }
        }

        // Floating Action Controls: Center Location
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .glassmorphic(
                        cornerRadius = 24.dp,
                        backgroundColor = Color(0xEE131A29),
                        borderColor = GlassBorderSubtle
                    )
                    .clickable {
                        viewModel.updateCurrentLocation(currentLocation.first, currentLocation.second)
                    }
                    .testTag("center_location_fab"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Center GPS",
                    tint = HyperLime,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Bottom Route & Elevation Sliding Card
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (selectedRoute != null) {
                // Route Planning Bottom Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassmorphic(
                            cornerRadius = 32.dp,
                            backgroundColor = Color(0xF2121826),
                            borderColor = GlassBorderSubtle,
                            elevation = 16.dp
                        )
                        .padding(20.dp)
                        .testTag("route_summary_card")
                ) {
                    Column {
                        // Quick Route Alternative Chips & Tags
                        routePlan?.routes?.let { routes ->
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                itemsIndexed(routes) { idx, r ->
                                    val isSelected = idx == selectedIndex
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(
                                                if (isSelected) HyperLime else Color(0x22FFFFFF)
                                            )
                                            .clickable { viewModel.selectRouteIndex(idx) }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = if (r.isRecommendedFlat) "🌱 FLATEST ROUTE" else if (idx == 0) "⚡ FASTEST" else "🛡️ LOW TRAFFIC",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            color = if (isSelected) ObsidianNavy else PureWhite,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Route Destination & Difficulty Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = destinationAddress.ifEmpty { "Target Destination" },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PureWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = selectedRoute.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }

                            // Grade Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (selectedRoute.difficulty == "Flat & Easy") HyperLime.copy(alpha = 0.18f) else StravaOrange.copy(alpha = 0.18f)
                                    )
                                    .border(
                                        1.dp,
                                        if (selectedRoute.difficulty == "Flat & Easy") HyperLime.copy(alpha = 0.4f) else StravaOrange.copy(alpha = 0.4f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = selectedRoute.difficulty.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (selectedRoute.difficulty == "Flat & Easy") HyperLime else StravaOrange
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // High-contrast 3-col telemetry: Distance, Time, Elevation Gain
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("DISTANCE", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "%.1f".format(selectedRoute.distanceKm),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = PureWhite
                                    )
                                    Text(" km", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.padding(bottom = 4.dp))
                                }
                            }

                            Column {
                                Text("EST. TIME", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${selectedRoute.durationMinutes}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = CyberCyan
                                    )
                                    Text(" min", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.padding(bottom = 4.dp))
                                }
                            }

                            Column {
                                Text("ELEV GAIN", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "+%.0f".format(selectedRoute.elevationGainMeters),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = StravaOrange
                                    )
                                    Text(" m", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.padding(bottom = 4.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Interactive Canvas Elevation Profile Chart
                        ElevationChart(
                            elevationProfile = selectedRoute.elevationProfile,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Pill Button: START NAVIGATION
                        Button(
                            onClick = {
                                showCountdown = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(12.dp, RoundedCornerShape(28.dp), spotColor = HyperLime)
                                .testTag("start_ride_button"),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HyperLime,
                                contentColor = ObsidianNavy
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "START NAVIGATION",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            } else {
                // Quick Launch Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassmorphic(
                            cornerRadius = 28.dp,
                            backgroundColor = Color(0xF0121826),
                            borderColor = GlassBorderSubtle
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Free Ride Tracking",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = PureWhite
                            )
                            Text(
                                text = "Track speed, distance & grade in background",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        Button(
                            onClick = {
                                showCountdown = true
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HyperLime,
                                contentColor = ObsidianNavy
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = HyperLime)
                                .testTag("quick_start_ride_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBike,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RIDE NOW",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black
                            )
                        }
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
                    onNavigateToTracking()
                },
                onCancel = {
                    showCountdown = false
                },
                onLocationReady = { lat, lon ->
                    viewModel.updateCurrentLocation(lat, lon)
                }
            )
        }
    }
}
