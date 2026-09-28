package com.example.export

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
import android.util.Log
import androidx.core.content.FileProvider
import com.example.model.TrackPoint
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SocialStoryExporter {

    private const val STORY_WIDTH = 1080
    private const val STORY_HEIGHT = 1920

    enum class StoryStyle {
        TRANSPARENT_OVERLAY,
        POSTER_GRAPHIC
    }

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
            val bitmap = when (style) {
                StoryStyle.TRANSPARENT_OVERLAY -> renderTransparentOverlay(
                    points = points,
                    distanceKm = distanceKm,
                    durationSeconds = durationSeconds,
                    avgSpeedKmh = avgSpeedKmh,
                    elevationGainM = elevationGainM
                )
                StoryStyle.POSTER_GRAPHIC -> renderPosterGraphic(
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

            val imagesDir = File(context.cacheDir, "images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            val prefix = if (style == StoryStyle.TRANSPARENT_OVERLAY) "story_overlay" else "story_poster"
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
                putExtra(Intent.EXTRA_SUBJECT, "BikeRoute Activity Story: $title")
                putExtra(Intent.EXTRA_TEXT, "Cycling ride recorded with BikeRoute! 🚴\nDistance: ${"%.2f".format(distanceKm)} km • Time: ${formatDuration(durationSeconds)}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Story via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            return true
        } catch (e: Exception) {
            Log.e("SocialStoryExporter", "Failed to generate story: ${e.message}", e)
            return false
        }
    }

    private fun renderTransparentOverlay(
        points: List<TrackPoint>,
        distanceKm: Double,
        durationSeconds: Long,
        avgSpeedKmh: Double,
        elevationGainM: Double
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(STORY_WIDTH, STORY_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        // Background is completely transparent

        // Draw Route in the center
        val routeBounds = RectF(120f, 380f, 960f, 1220f)
        drawProjectedRoute(
            canvas = canvas,
            points = points,
            bounds = routeBounds,
            strokeColor = 0xFF10B981.toInt(), // Neon Emerald
            glowColor = 0x8806B6D4.toInt(),   // Glow Cyan
            strokeWidth = 14f
        )

        // Draw Translucent Glassmorphic Metric Badges at bottom
        val cardRect = RectF(80f, 1300f, 1000f, 1680f)
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(190, 15, 23, 42) // Dark translucent slate
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(120, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(cardRect, 48f, 48f, cardPaint)
        canvas.drawRoundRect(cardRect, 48f, 48f, borderPaint)

        // 2x2 Telemetry inside badge
        drawMetric(canvas, "DISTANCE", "%.2f km".format(distanceKm), 140f, 1420f)
        drawMetric(canvas, "TIME", formatDuration(durationSeconds), 580f, 1420f)
        drawMetric(canvas, "AVG SPEED", "%.1f km/h".format(avgSpeedKmh), 140f, 1580f)
        drawMetric(canvas, "ELEVATION GAIN", "+%.0f m".format(elevationGainM), 580f, 1580f)

        // Watermark logo top
        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(8f, 0f, 4f, Color.argb(180, 0, 0, 0))
        }
        canvas.drawText("BIKEROUTE 🚴", 100f, 180f, brandPaint)

        return bitmap
    }

    private fun renderPosterGraphic(
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

        // Background Athletic Gradient
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, STORY_HEIGHT.toFloat(),
                intArrayOf(
                    Color.rgb(15, 23, 42),   // Dark Navy #0F172A
                    Color.rgb(24, 34, 53),   // Mid Slate
                    Color.rgb(11, 17, 32)    // Deep Midnight #0B1120
                ),
                floatArrayOf(0f, 0.45f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, STORY_WIDTH.toFloat(), STORY_HEIGHT.toFloat(), bgPaint)

        // Geometric tech lines in background
        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(16, 255, 255, 255)
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        for (y in 200..1800 step 160) {
            canvas.drawLine(60f, y.toFloat(), 1020f, y.toFloat(), gridPaint)
        }

        // Header Section
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 58f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF10B981.toInt() // Emerald
            textSize = 28f
            letterSpacing = 0.15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(148, 163, 184) // Slate 400
            textSize = 28f
        }

        canvas.drawText("BIKEROUTE // RIDE TELEMETRY", 80f, 150f, subPaint)
        canvas.drawText(title.take(24), 80f, 225f, titlePaint)

        val dateStr = SimpleDateFormat("EEEE, MMM d, yyyy • h:mm a", Locale.US).format(Date(startTime))
        canvas.drawText(dateStr, 80f, 280f, datePaint)

        // Route Map Snippet Box in the center
        val mapBoxRect = RectF(80f, 340f, 1000f, 1080f)
        val mapBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(160, 30, 41, 59)
            style = Paint.Style.FILL
        }
        val mapBoxStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(60, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(mapBoxRect, 36f, 36f, mapBoxPaint)
        canvas.drawRoundRect(mapBoxRect, 36f, 36f, mapBoxStroke)

        // Draw Route inside the Map Box
        val routeInnerBounds = RectF(140f, 400f, 940f, 1020f)
        drawProjectedRoute(
            canvas = canvas,
            points = points,
            bounds = routeInnerBounds,
            strokeColor = 0xFF10B981.toInt(),
            glowColor = 0x9906B6D4.toInt(),
            strokeWidth = 14f
        )

        // Telemetry Grid (6 Metrics)
        val gridY1 = 1140f
        val gridY2 = 1320f
        val gridY3 = 1500f

        drawStatPill(canvas, 80f, gridY1, 440f, "DISTANCE", "%.2f km".format(distanceKm), 0xFF10B981.toInt())
        drawStatPill(canvas, 560f, gridY1, 440f, "TIME", formatDuration(durationSeconds), 0xFF06B6D4.toInt())

        drawStatPill(canvas, 80f, gridY2, 440f, "AVG SPEED", "%.1f km/h".format(avgSpeedKmh), Color.WHITE)
        drawStatPill(canvas, 560f, gridY2, 440f, "MAX SPEED", "%.1f km/h".format(maxSpeedKmh), Color.WHITE)

        drawStatPill(canvas, 80f, gridY3, 440f, "ELEVATION GAIN", "+%.0f m".format(elevationGainM), 0xFFF59E0B.toInt())
        drawStatPill(canvas, 560f, gridY3, 440f, "ENERGY", "$calories kcal", 0xFFF43F5E.toInt())

        // Bottom Footer
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            textSize = 26f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Tracked with BikeRoute • Zero-Cost / Open Source GIS", 540f, 1780f, footerPaint)

        return bitmap
    }

    private fun drawStatPill(
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
            color = Color.argb(180, 30, 41, 59)
            style = Paint.Style.FILL
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(40, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(rect, 24f, 24f, bgPaint)
        canvas.drawRoundRect(rect, 24f, 24f, strokePaint)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(148, 163, 184)
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.05f
        }
        canvas.drawText(label, x + 30f, y + 46f, labelPaint)

        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = valueColor
            textSize = 46f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(value, x + 30f, y + 108f, valuePaint)
    }

    private fun drawMetric(canvas: Canvas, label: String, value: String, x: Float, y: Float) {
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(148, 163, 184)
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(label, x, y, labelPaint)
        canvas.drawText(value, x, y + 60f, valuePaint)
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
            // Draw placeholder curve if no points
            val dummyPath = Path().apply {
                moveTo(bounds.left + 50f, bounds.bottom - 80f)
                cubicTo(
                    bounds.left + 200f, bounds.top + 60f,
                    bounds.right - 200f, bounds.bottom - 40f,
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

        // Prevent division by zero for single-point or straight-line routes
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

        // Aspect ratio preservation
        val boundW = bounds.width()
        val boundH = bounds.height()
        val scaleX = boundW / dLon
        val scaleY = boundH / dLat
        val scale = minOf(scaleX, scaleY)

        val offsetX = bounds.left + (boundW - dLon * scale) / 2f
        val offsetY = bounds.top + (boundH - dLat * scale) / 2f

        fun project(pt: TrackPoint): Pair<Float, Float> {
            val px = (offsetX + (pt.longitude - minLon) * scale).toFloat()
            // Invert latitude for Y-axis (North is up)
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

        // Draw glow layer
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = glowColor
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth * 2.2f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        canvas.drawPath(path, glowPaint)

        // Draw main route polyline
        val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = strokeColor
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        canvas.drawPath(path, mainPaint)

        // Draw Start Pin (Neon Green circle)
        val startPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF10B981.toInt()
            style = Paint.Style.FILL
        }
        val pinBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            this.strokeWidth = 6f
        }
        canvas.drawCircle(firstProj.first, firstProj.second, 20f, startPaint)
        canvas.drawCircle(firstProj.first, firstProj.second, 20f, pinBorder)

        // Draw End Pin (Amber / Coral circle)
        val lastProj = project(points.last())
        val endPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF43F5E.toInt()
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
