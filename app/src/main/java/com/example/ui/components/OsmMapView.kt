package com.example.ui.components

import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    centerPoint: Pair<Double, Double>? = null,
    zoomLevel: Double = 15.0,
    routePoints: List<Pair<Double, Double>> = emptyList(),
    destinationPoint: Pair<Double, Double>? = null,
    currentLocationPoint: Pair<Double, Double>? = null,
    onMapClick: ((lat: Double, lon: Double) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(zoomLevel)
            isTilesScaledToDpi = true
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView.onDetach()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    // Handle map tap events
    DisposableEffect(onMapClick) {
        val overlay = if (onMapClick != null) {
            MapEventsOverlay(object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                    onMapClick(p.latitude, p.longitude)
                    return true
                }
                override fun longPressHelper(p: GeoPoint): Boolean = false
            })
        } else null

        if (overlay != null) {
            mapView.overlays.add(0, overlay)
        }
        onDispose {
            if (overlay != null) {
                mapView.overlays.remove(overlay)
            }
        }
    }

    // Update markers and polyline
    LaunchedEffect(routePoints, destinationPoint, currentLocationPoint) {
        // Clear previous overlays except tap receiver
        val toRemove = mapView.overlays.filter { it !is MapEventsOverlay }
        mapView.overlays.removeAll(toRemove)

        // Draw Route Polyline
        if (routePoints.isNotEmpty()) {
            val geoPoints = routePoints.map { GeoPoint(it.first, it.second) }

            // Glow line underneath
            val glowLine = Polyline(mapView).apply {
                setPoints(geoPoints)
                outlinePaint.color = Color.parseColor("#4406B6D4")
                outlinePaint.strokeWidth = 22f
                outlinePaint.strokeCap = Paint.Cap.ROUND
                outlinePaint.strokeJoin = Paint.Join.ROUND
            }
            mapView.overlays.add(glowLine)

            // Main sharp polyline
            val routeLine = Polyline(mapView).apply {
                setPoints(geoPoints)
                outlinePaint.color = Color.parseColor("#10B981") // Athletic Emerald
                outlinePaint.strokeWidth = 12f
                outlinePaint.strokeCap = Paint.Cap.ROUND
                outlinePaint.strokeJoin = Paint.Join.ROUND
            }
            mapView.overlays.add(routeLine)

            // Zoom map to fit the route bounding box
            if (geoPoints.size >= 2) {
                var maxLat = -90.0
                var minLat = 90.0
                var maxLon = -180.0
                var minLon = 180.0
                for (pt in geoPoints) {
                    if (pt.latitude > maxLat) maxLat = pt.latitude
                    if (pt.latitude < minLat) minLat = pt.latitude
                    if (pt.longitude > maxLon) maxLon = pt.longitude
                    if (pt.longitude < minLon) minLon = pt.longitude
                }
                val box = BoundingBox(
                    maxLat + 0.005,
                    maxLon + 0.005,
                    minLat - 0.005,
                    minLon - 0.005
                )
                mapView.zoomToBoundingBox(box, true, 80)
            }
        }

        // Draw Destination Marker
        destinationPoint?.let { dest ->
            val destMarker = Marker(mapView).apply {
                position = GeoPoint(dest.first, dest.second)
                title = "Destination"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            mapView.overlays.add(destMarker)
        }

        // Draw Current Location Marker
        currentLocationPoint?.let { curr ->
            val locMarker = Marker(mapView).apply {
                position = GeoPoint(curr.first, curr.second)
                title = "Your Location"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            }
            mapView.overlays.add(locMarker)
        }

        mapView.invalidate()
    }

    // Center map if specified and no route
    LaunchedEffect(centerPoint) {
        if (centerPoint != null && routePoints.isEmpty()) {
            mapView.controller.animateTo(GeoPoint(centerPoint.first, centerPoint.second))
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}
