package com.example.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.example.model.TrackPoint
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SocialStoryExporter {

    const val STORY_WIDTH = 1080
    const val STORY_HEIGHT = 1920

    enum class StoryStyle {
        CYBER_NEON_POSTER,
        MINIMALIST_HUD_OVERLAY,
        ATHLETIC_PERFORMANCE_CARD
    }

    /**
     * Generates a 1080x1920 (9:16) Bitmap for in-app preview or export.
     */
    fun generateBitmap(
        style: StoryStyle,
        title: String,
        distanceKm: Double,
        durationSeconds: Long,
        avgSpeedKmh: Double,
        maxSpeedKmh: Double,
        elevationGainM: Double,
        calories: Int,
        startTime: Long,
        points: List<TrackPoint>
    ): Bitmap {
        return when (style) {
            StoryStyle.CYBER_NEON_POSTER -> renderCyberNeonPoster(
                title = title,
                distanceKm = distanceKm,
                durationSeconds = durationSeconds,
                avgSpeedKmh = avgSpeedKmh,
                elevationGainM = elevationGainM,
                startTime = startTime,
                points = points
            )
            StoryStyle.MINIMALIST_HUD_OVERLAY -> renderMinimalistHudOverlay(
                points = points,
                distanceKm = distanceKm,
                durationSeconds = durationSeconds,
                avgSpeedKmh = avgSpeedKmh,
                elevationGainM = elevationGainM
            )
            StoryStyle.ATHLETIC_PERFORMANCE_CARD -> renderAthleticPerformanceCard(
                title = title,
                distanceKm = distanceKm,
                durationSeconds = durationSeconds,
                avgSpeedKmh = avgSpeedKmh,
                maxSpeedKmh = maxSpeedKmh,
                elevationGainM = elevationGainM,
                calories = calories,
                startTime = startTime,
                points = points
            )
        }
    }

    /**
     * Downloads/saves the generated 9:16 story image directly to the user's Gallery (Pictures/BikeRoute).
     */
    fun saveStoryToGallery(
        context: Context,
        style: StoryStyle,
        title: String,
        distanceKm: Double,
        durationSeconds: Long,
        avgSpeedKmh: Double,
        maxSpeedKmh: Double,
        elevationGainM: Double,
        calories: Int,
        startTime: Long,
        points: List<TrackPoint>
    ): Boolean {
        return try {
            val bitmap = generateBitmap(
                style = style,
                title = title,
                distanceKm = distanceKm,
                durationSeconds = durationSeconds,
                avgSpeedKmh = avgSpeedKmh,
                maxSpeedKmh = maxSpeedKmh,
                elevationGainM = elevationGainM,
                calories = calories,
                startTime = startTime,
                points = points
            )

            val fileName = "bikeroute_${style.name.lowercase()}_${System.currentTimeMillis()}"
            val resolver = context.contentResolver

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.png")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BikeRoute")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return false

            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            true
        } catch (e: Exception) {
            Log.e("SocialStoryExporter", "Failed to save image to gallery: ${e.message}", e)
            false
        }
    }

    /**
     * Exports the 9:16 story image and fires an ACTION_SEND Intent chooser (Instagram, Stories, WhatsApp).
     */
    fun exportAndShareStory(
        context: Context,
        style: StoryStyle,
        title: String,
        distanceKm: Double,
        durationSeconds: Long,
        avgSpeedKmh: Double,
        maxSpeedKmh: Double,
        elevationGainM: Double,
        calories: Int,
        startTime: Long,
        points: List<TrackPoint>
    ): Boolean {
        try {
            val bitmap = generateBitmap(
                style = style,
                title = title,
                distanceKm = distanceKm,
                durationSeconds = durationSeconds,
                avgSpeedKmh = avgSpeedKmh,
                maxSpeedKmh = maxSpeedKmh,
                elevationGainM = elevationGainM,
                calories = calories,
                startTime = startTime,
                points = points
            )

            val imagesDir = File(context.cacheDir, "images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            val prefix = style.name.lowercase()
            val file = File(imagesDir, "${prefix}_${System.currentTimeMillis()}.png")

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "BikeRoute Story: $title")
                putExtra(Intent.EXTRA_TEXT, "Cycling ride tracked with BikeRoute! 🚴\nDistance: ${"%.2f".format(distanceKm)} km • Time: ${formatDuration(durationSeconds)}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Story to Instagram")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            return true
        } catch (e: Exception) {
            Log.e("SocialStoryExporter", "Failed to generate story: ${e.message}", e)
            return false
        }
    }

    // ==========================================
    // OPTION 1: CYBER NEON POSTER (DARK & EDGY)
    // ==========================================
    private fun renderCyberNeonPoster(
        title: String,
        distanceKm: Double,
        durationSeconds: Long,
        avgSpeedKmh: Double,
        elevationGainM: Double,
        startTime: Long,
        points: List<TrackPoint>
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(STORY_WIDTH, STORY_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Deep gradient canvas (#0B0F17 to #1E293B)
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, STORY_HEIGHT.toFloat(),
                intArrayOf(
                    Color.rgb(11, 15, 23),   // #0B0F17
                    Color.rgb(19, 26, 41),   // Mid Obsidian
                    Color.rgb(30, 41, 59)    // #1E293B
                ),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, STORY_WIDTH.toFloat(), STORY_HEIGHT.toFloat(), bgPaint)

        // Subtle background athletic grid lines
        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(14, 255, 255, 255)
            strokeWidth = 2f
        }
        for (y in 240..1700 step 180) {
            canvas.drawLine(60f, y.toFloat(), 1020f, y.toFloat(), gridPaint)
        }

        // Top Header
        val brandTag = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFD4FF00.toInt() // Hyper Lime
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.2f
        }
        canvas.drawText("BIKEROUTE // CYBER TELEMETRY", 90f, 160f, brandTag)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 58f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(title.take(24), 90f, 236f, titlePaint)

        val dateStr = SimpleDateFormat("EEEE, MMM d • h:mm a", Locale.US).format(Date(startTime))
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(148, 163, 184)
            textSize = 28f
        }
        canvas.drawText(dateStr, 90f, 290f, datePaint)

        // Center Route Artwork (Neon Glow Polyline)
        val routeBounds = RectF(100f, 360f, 980f, 1180f)
        drawProjectedRoute(
            canvas = canvas,
            points = points,
            bounds = routeBounds,
            strokeColor = 0xFFD4FF00.toInt(), // Hyper Lime
            glowColor = 0x88FF5722.toInt(),   // Strava Orange Glow
            strokeWidth = 16f
        )

        // Bold Typography Overlay Badges at bottom
        val cardRect = RectF(80f, 1260f, 1000f, 1660f)
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(210, 19, 26, 41)
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(60, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        canvas.drawRoundRect(cardRect, 40f, 40f, cardPaint)
        canvas.drawRoundRect(cardRect, 40f, 40f, borderPaint)

        // Telemetry Grid
        drawBigMetric(canvas, "DISTANCE", "%.2f km".format(distanceKm), 140f, 1370f, 0xFFD4FF00.toInt())
        drawBigMetric(canvas, "DURATION", formatDuration(durationSeconds), 580f, 1370f, Color.WHITE)
        drawBigMetric(canvas, "AVG SPEED", "%.1f km/h".format(avgSpeedKmh), 140f, 1530f, 0xFF00F2FE.toInt())
        drawBigMetric(canvas, "ELEV GAIN", "+%.0f m".format(elevationGainM), 580f, 1530f, 0xFFFF5722.toInt())

        // Bottom Minimalist BIKEROUTE Watermark
        val watermark = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            textSize = 28f
            letterSpacing = 0.25f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("BIKEROUTE • RIDE. TRACK. SHARE.", 540f, 1780f, watermark)

        return bitmap
    }

    // ==========================================
    // OPTION 2: MINIMALIST HUD OVERLAY (TRANSPARENT)
    // ==========================================
    private fun renderMinimalistHudOverlay(
        points: List<TrackPoint>,
        distanceKm: Double,
        durationSeconds: Long,
        avgSpeedKmh: Double,
        elevationGainM: Double
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(STORY_WIDTH, STORY_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        // Background is completely transparent for overlaying on user's riding photos

        // Glowing center route artwork
        val routeBounds = RectF(120f, 380f, 960f, 1200f)
        drawProjectedRoute(
            canvas = canvas,
            points = points,
            bounds = routeBounds,
            strokeColor = 0xFFD4FF00.toInt(),
            glowColor = 0xAA00F2FE.toInt(),
            strokeWidth = 16f
        )

        // Translucent Frosted Glass HUD Card with Drop Shadow
        val hudRect = RectF(80f, 1280f, 1000f, 1680f)
        val hudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(195, 11, 15, 23)
            style = Paint.Style.FILL
            setShadowLayer(32f, 0f, 12f, Color.argb(160, 0, 0, 0))
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(90, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(hudRect, 48f, 48f, hudPaint)
        canvas.drawRoundRect(hudRect, 48f, 48f, borderPaint)

        // Clean white typography with drop shadows
        drawBigMetric(canvas, "DISTANCE", "%.2f km".format(distanceKm), 140f, 1400f, Color.WHITE)
        drawBigMetric(canvas, "TIME", formatDuration(durationSeconds), 580f, 1400f, 0xFFD4FF00.toInt())
        drawBigMetric(canvas, "AVG SPEED", "%.1f km/h".format(avgSpeedKmh), 140f, 1560f, Color.WHITE)
        drawBigMetric(canvas, "ELEV GAIN", "+%.0f m".format(elevationGainM), 580f, 1560f, 0xFFFF5722.toInt())

        // Top watermark badge
        val watermark = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(10f, 0f, 4f, Color.BLACK)
        }
        canvas.drawText("BIKEROUTE 🚴", 100f, 180f, watermark)

        return bitmap
    }

    // ==========================================
    // OPTION 3: ATHLETIC PERFORMANCE CARD (CLEAN & MODERN)
    // ==========================================
    private fun renderAthleticPerformanceCard(
        title: String,
        distanceKm: Double,
        durationSeconds: Long,
        avgSpeedKmh: Double,
        maxSpeedKmh: Double,
        elevationGainM: Double,
        calories: Int,
        startTime: Long,
        points: List<TrackPoint>
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(STORY_WIDTH, STORY_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Solid Athletic Obsidian Canvas
        val bgPaint = Paint().apply {
            color = Color.rgb(11, 15, 23)
        }
        canvas.drawRect(0f, 0f, STORY_WIDTH.toFloat(), STORY_HEIGHT.toFloat(), bgPaint)

        // Top Card Frame: Map Snippet Snapshot (80..760)
        val mapBox = RectF(70f, 140f, 1010f, 760f)
        val mapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(19, 26, 41)
            style = Paint.Style.FILL
        }
        val mapBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(50, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        canvas.drawRoundRect(mapBox, 36f, 36f, mapPaint)
        canvas.drawRoundRect(mapBox, 36f, 36f, mapBorder)

        // Draw Map Route in top card
        val mapRouteBounds = RectF(120f, 200f, 960f, 700f)
        drawProjectedRoute(
            canvas = canvas,
            points = points,
            bounds = mapRouteBounds,
            strokeColor = 0xFFD4FF00.toInt(),
            glowColor = 0x88FF5722.toInt(),
            strokeWidth = 14f
        )

        // Activity Title & Date Banner
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(title.take(24), 80f, 850f, titlePaint)

        val dateStr = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US).format(Date(startTime))
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(148, 163, 184)
            textSize = 28f
        }
        canvas.drawText(dateStr, 80f, 895f, datePaint)

        // Middle Section: Split Metrics Table (940..1440)
        val gridY1 = 940f
        val gridY2 = 1110f
        val gridY3 = 1280f

        drawMetricBox(canvas, 70f, gridY1, 450f, "DISTANCE", "%.2f km".format(distanceKm), 0xFFD4FF00.toInt())
        drawMetricBox(canvas, 560f, gridY1, 450f, "TIME", formatDuration(durationSeconds), Color.WHITE)

        drawMetricBox(canvas, 70f, gridY2, 450f, "AVG SPEED", "%.1f km/h".format(avgSpeedKmh), Color.WHITE)
        drawMetricBox(canvas, 560f, gridY2, 450f, "MAX SPEED", "%.1f km/h".format(maxSpeedKmh), 0xFF00F2FE.toInt())

        drawMetricBox(canvas, 70f, gridY3, 450f, "ELEV GAIN", "+%.0f m".format(elevationGainM), 0xFFFF5722.toInt())
        drawMetricBox(canvas, 560f, gridY3, 450f, "ENERGY", "$calories kcal", Color.WHITE)

        // Bottom Section: Elevation Profile Wave Graph (1460..1720)
        val chartBox = RectF(70f, 1460f, 1010f, 1720f)
        canvas.drawRoundRect(chartBox, 28f, 28f, mapPaint)
        canvas.drawRoundRect(chartBox, 28f, 28f, mapBorder)

        drawMiniElevationWave(canvas, points, RectF(100f, 1490f, 980f, 1690f))

        // Bottom Watermark
        val brand = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            textSize = 26f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("PERFORMANCE TELEMETRY • BIKEROUTE", 540f, 1790f, brand)

        return bitmap
    }

    private fun drawMetricBox(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        label: String,
        value: String,
        valueColor: Int
    ) {
        val rect = RectF(x, y, x + width, y + 140f)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(19, 26, 41)
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(40, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(rect, 24f, 24f, bgPaint)
        canvas.drawRoundRect(rect, 24f, 24f, borderPaint)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(148, 163, 184)
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.08f
        }
        canvas.drawText(label, x + 28f, y + 46f, labelPaint)

        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = valueColor
            textSize = 46f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(value, x + 28f, y + 108f, valuePaint)
    }

    private fun drawBigMetric(canvas: Canvas, label: String, value: String, x: Float, y: Float, valueColor: Int) {
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(148, 163, 184)
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.08f
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = valueColor
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(label, x, y, labelPaint)
        canvas.drawText(value, x, y + 62f, valuePaint)
    }

    private fun drawMiniElevationWave(canvas: Canvas, points: List<TrackPoint>, bounds: RectF) {
        val elevations = if (points.size >= 2) points.map { it.altitude } else listOf(30.0, 45.0, 50.0, 38.0, 60.0)
        val minElev = elevations.minOrNull() ?: 0.0
        val maxElev = elevations.maxOrNull() ?: 100.0
        val range = (maxElev - minElev).coerceAtLeast(10.0)

        val stepX = bounds.width() / (elevations.size - 1).coerceAtLeast(1)
        val path = Path()
        val fillPath = Path()

        elevations.forEachIndexed { i, elev ->
            val normY = ((elev - minElev) / range).toFloat()
            val px = bounds.left + i * stepX
            val py = bounds.bottom - (normY * (bounds.height() - 20f)) - 10f

            if (i == 0) {
                path.moveTo(px, py)
                fillPath.moveTo(px, bounds.bottom)
                fillPath.lineTo(px, py)
            } else {
                path.lineTo(px, py)
                fillPath.lineTo(px, py)
            }
        }
        fillPath.lineTo(bounds.right, bounds.bottom)
        fillPath.close()

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, bounds.top, 0f, bounds.bottom,
                intArrayOf(Color.argb(80, 212, 255, 0), Color.argb(5, 212, 255, 0)),
                null,
                Shader.TileMode.CLAMP
            )
            style = Paint.Style.FILL
        }
        canvas.drawPath(fillPath, fillPaint)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFD4FF00.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 4f
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawPath(path, strokePaint)
    }

    private fun drawProjectedRoute(
        canvas: Canvas,
        points: List<TrackPoint>,
        bounds: RectF,
        strokeColor: Int,
        glowColor: Int,
        strokeWidth: Float
    ) {
        if (points.isEmpty()) {
            val dummyPath = Path().apply {
                moveTo(bounds.left + 50f, bounds.bottom - 80f)
                cubicTo(
                    bounds.left + 220f, bounds.top + 60f,
                    bounds.right - 220f, bounds.bottom - 40f,
                    bounds.right - 50f, bounds.top + 80f
                )
            }
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = strokeColor
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
            }
            canvas.drawPath(dummyPath, paint)
            return
        }

        var minLat = points.minOf { it.latitude }
        var maxLat = points.maxOf { it.latitude }
        var minLon = points.minOf { it.longitude }
        var maxLon = points.maxOf { it.longitude }

        if (maxLat - minLat < 0.0001) {
            minLat -= 0.0005
            maxLat += 0.0005
        }
        if (maxLon - minLon < 0.0001) {
            minLon -= 0.0005
            maxLon += 0.0005
        }

        val dLat = maxLat - minLat
        val dLon = maxLon - minLon

        val boundW = bounds.width()
        val boundH = bounds.height()
        val scaleX = boundW / dLon
        val scaleY = boundH / dLat
        val scale = minOf(scaleX, scaleY)

        val offsetX = bounds.left + (boundW - dLon * scale) / 2f
        val offsetY = bounds.top + (boundH - dLat * scale) / 2f

        fun project(pt: TrackPoint): Pair<Float, Float> {
            val px = (offsetX + (pt.longitude - minLon) * scale).toFloat()
            val py = (offsetY + (maxLat - pt.latitude) * scale).toFloat()
            return Pair(px, py)
        }

        val path = Path()
        val firstProj = project(points.first())
        path.moveTo(firstProj.first, firstProj.second)

        for (i in 1 until points.size) {
            val proj = project(points[i])
            path.lineTo(proj.first, proj.second)
        }

        // Glow Layer
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = glowColor
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth * 2.4f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        canvas.drawPath(path, glowPaint)

        // Core Sharp Neon Path
        val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = strokeColor
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        canvas.drawPath(path, mainPaint)

        // Start Pin (Green)
        val startPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFD4FF00.toInt()
            style = Paint.Style.FILL
        }
        val pinBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            this.strokeWidth = 6f
        }
        canvas.drawCircle(firstProj.first, firstProj.second, 22f, startPaint)
        canvas.drawCircle(firstProj.first, firstProj.second, 22f, pinBorder)

        // Finish Pin (Strava Orange)
        val lastProj = project(points.last())
        val endPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFF5722.toInt()
            style = Paint.Style.FILL
        }
        canvas.drawCircle(lastProj.first, lastProj.second, 22f, endPaint)
        canvas.drawCircle(lastProj.first, lastProj.second, 22f, pinBorder)
    }

    private fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }
}
