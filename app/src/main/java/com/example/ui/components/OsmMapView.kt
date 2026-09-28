package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
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
    zoomLevel: Double = 15.5,
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

            // Sleek Dark Matrix: Inverts brightness and shifts to obsidian-navy aesthetic
            val colorMatrix = ColorMatrix(
                floatArrayOf(
                    -0.85f, 0f, 0f, 0f, 210f,
                    0f, -0.85f, 0f, 0f, 215f,
                    0f, 0f, -0.80f, 0f, 230f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            val filter = ColorMatrixColorFilter(colorMatrix)
            overlayManager.tilesOverlay.setColorFilter(filter)
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

    // Tap to set destination
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

    // Cache clean markers
    val locationPuckIcon = remember(context) { createCleanLocationPuck(context) }
    val destinationIcon = remember(context) { createCleanDestinationMarker(context) }
    val startNodeIcon = remember(context) { createCleanStartNodeMarker(context) }

    // Update polylines and clean markers
    LaunchedEffect(routePoints, destinationPoint, currentLocationPoint) {
        val toRemove = mapView.overlays.filter { it !is MapEventsOverlay }
        mapView.overlays.removeAll(toRemove)

        // ==========================================
        // CLEAN POLYLINE RENDERING
        // ==========================================
        if (routePoints.isNotEmpty()) {
            val geoPoints = routePoints.map { GeoPoint(it.first, it.second) }

            // 1. Subtle Ambient Underglow (prevents harsh pixelation on dark maps)
            val ambientUnderglow = Polyline(mapView).apply {
                setPoints(geoPoints)
                outlinePaint.color = Color.parseColor("#2BD4FF00") // 17% Hyper Lime glow
                outlinePaint.strokeWidth = 20f
                outlinePaint.strokeCap = Paint.Cap.ROUND
                outlinePaint.strokeJoin = Paint.Join.ROUND
                outlinePaint.isAntiAlias = true
            }
            mapView.overlays.add(ambientUnderglow)

            // 2. Crisp Structural Casing (Dark outline for sharp contrast)
            val casingPolyline = Polyline(mapView).apply {
                setPoints(geoPoints)
                outlinePaint.color = Color.parseColor("#CC0B0F17") // Obsidian casing
                outlinePaint.strokeWidth = 13f
                outlinePaint.strokeCap = Paint.Cap.ROUND
                outlinePaint.strokeJoin = Paint.Join.ROUND
                outlinePaint.isAntiAlias = true
            }
            mapView.overlays.add(casingPolyline)

            // 3. Core Vibrant Clean Polyline (Razor-sharp Hyper Lime)
            val corePolyline = Polyline(mapView).apply {
                setPoints(geoPoints)
                outlinePaint.color = Color.parseColor("#D4FF00") // Core Hyper Lime
                outlinePaint.strokeWidth = 8.5f
                outlinePaint.strokeCap = Paint.Cap.ROUND
                outlinePaint.strokeJoin = Paint.Join.ROUND
                outlinePaint.isAntiAlias = true
            }
            mapView.overlays.add(corePolyline)

            // Start Route Node Marker (Clean Green Node)
            if (geoPoints.size >= 2) {
                val startMarker = Marker(mapView).apply {
                    position = geoPoints.first()
                    icon = startNodeIcon
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    title = "Start Point"
                }
                mapView.overlays.add(startMarker)
            }

            // Center / Fit bounding box
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
                mapView.zoomToBoundingBox(box, true, 110)
            }
        }

        // ==========================================
        // CLEAN DESTINATION PIN
        // ==========================================
        destinationPoint?.let { dest ->
            val destMarker = Marker(mapView).apply {
                position = GeoPoint(dest.first, dest.second)
                icon = destinationIcon
                title = "Destination"
                setAnchor(0.5f, 0.95f) // Anchored precisely at pointer tip
            }
            mapView.overlays.add(destMarker)
        }

        // ==========================================
        // CLEAN GPS LOCATION PUCK
        // ==========================================
        currentLocationPoint?.let { curr ->
            val locMarker = Marker(mapView).apply {
                position = GeoPoint(curr.first, curr.second)
                icon = locationPuckIcon
                title = "Your Position"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            }
            mapView.overlays.add(locMarker)
        }

        mapView.invalidate()
    }

    // Auto-center if single position
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

/**
 * Creates a clean, modern athletic GPS location puck:
 * - Ambient pulse ring
 * - Crisp pure white border
 * - Electric Cyan / Lime core dot
 */
private fun createCleanLocationPuck(context: Context): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val sizePx = (44 * density).toInt()
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val center = sizePx / 2f

    // 1. Soft Ambient Radar Halo
    val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3300F2FE") // 20% Cyber Cyan
        style = Paint.Style.FILL
    }
    canvas.drawCircle(center, center, 20 * density, haloPaint)

    // 2. White Border Ring
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        setShadowLayer(4 * density, 0f, 2 * density, Color.parseColor("#80000000"))
    }
    canvas.drawCircle(center, center, 12 * density, borderPaint)

    // 3. Electric Cyan Core Puck
    val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00F2FE") // Cyber Cyan
        style = Paint.Style.FILL
    }
    canvas.drawCircle(center, center, 9 * density, corePaint)

    // 4. Ultra-clean center dot
    val centerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(center, center, 3 * density, centerDotPaint)

    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Creates a clean, modern athletic Destination pin:
 * - Strava Orange teardrop pin
 * - Crisp white border
 * - Inner white cyclist / target node
 * - Clean drop shadow
 */
private fun createCleanDestinationMarker(context: Context): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val widthPx = (38 * density).toInt()
    val heightPx = (50 * density).toInt()
    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val cx = widthPx / 2f
    val r = 16 * density
    val tipY = heightPx - (4 * density)

    // Shadow at tip
    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#55000000")
        style = Paint.Style.FILL
    }
    canvas.drawOval(RectF(cx - 8 * density, tipY - 2 * density, cx + 8 * density, tipY + 4 * density), shadowPaint)

    // Pin Body Path (Modern rounded teardrop)
    val pinPath = Path().apply {
        moveTo(cx, tipY)
        // Left curve to head
        cubicTo(
            cx - 6 * density, tipY - 14 * density,
            cx - r, r + 4 * density,
            cx - r, r
        )
        // Top arc
        arcTo(RectF(cx - r, 2 * density, cx + r, 2 * r + 2 * density), 180f, 180f, false)
        // Right curve back to tip
        cubicTo(
            cx + r, r + 4 * density,
            cx + 6 * density, tipY - 14 * density,
            cx, tipY
        )
        close()
    }

    // 1. Pure White Border
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawPath(pinPath, borderPaint)

    // 2. Strava Orange Inner Fill
    val innerR = r - (2.5f * density)
    val innerTipY = tipY - (3 * density)
    val innerPath = Path().apply {
        moveTo(cx, innerTipY)
        cubicTo(
            cx - 5 * density, innerTipY - 12 * density,
            cx - innerR, innerR + 4 * density,
            cx - innerR, innerR + 2 * density
        )
        arcTo(RectF(cx - innerR, 4.5f * density, cx + innerR, 2 * innerR + 4.5f * density), 180f, 180f, false)
        cubicTo(
            cx + innerR, innerR + 4 * density,
            cx + 5 * density, innerTipY - 12 * density,
            cx, innerTipY
        )
        close()
    }

    val orangePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF5722") // Electric Strava Orange
        style = Paint.Style.FILL
    }
    canvas.drawPath(innerPath, orangePaint)

    // 3. Clean White Inner Dot / Target Ring
    val dotCenterY = 2 * density + r
    val innerRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, dotCenterY, 5.5f * density, innerRingPaint)

    val innerDarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0B0F17")
        style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, dotCenterY, 2.5f * density, innerDarkPaint)

    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Creates a clean start node marker for route origin:
 * - Hyper Lime circle
 * - Pure white border
 */
private fun createCleanStartNodeMarker(context: Context): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val sizePx = (28 * density).toInt()
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val center = sizePx / 2f

    val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#33D4FF00")
        style = Paint.Style.FILL
    }
    canvas.drawCircle(center, center, 13 * density, haloPaint)

    val whiteBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(center, center, 9 * density, whiteBorder)

    val limeCore = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D4FF00")
        style = Paint.Style.FILL
    }
    canvas.drawCircle(center, center, 6.5f * density, limeCore)

    val darkDot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0B0F17")
        style = Paint.Style.FILL
    }
    canvas.drawCircle(center, center, 2.5f * density, darkDot)

    return BitmapDrawable(context.resources, bitmap)
}
