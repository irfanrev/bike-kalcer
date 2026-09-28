package com.example.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.example.model.TrackPoint
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object GpxExporter {

    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * Downloads/saves the GPX file directly to device Downloads/BikeRoute folder.
     */
    fun saveGpxToDownloads(
        context: Context,
        title: String,
        startTime: Long,
        points: List<TrackPoint>
    ): Boolean {
        return try {
            val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9_]"), "_").take(24)
            val fileName = "bikeroute_${sanitizedTitle}_${startTime}.gpx"
            val gpxContent = buildGpxXml(title, startTime, points)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/gpx+xml")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/BikeRoute")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return false
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(gpxContent.toByteArray(Charsets.UTF_8))
                }
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
                true
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDir = File(downloadsDir, "BikeRoute")
                if (!targetDir.exists()) targetDir.mkdirs()
                val targetFile = File(targetDir, fileName)
                targetFile.writeText(gpxContent, Charsets.UTF_8)
                true
            }
        } catch (e: Exception) {
            Log.e("GpxExporter", "Failed to save GPX to downloads: ${e.message}", e)
            false
        }
    }

    /**
     * Exports GPX to cache and launches intent chooser.
     */
    fun exportAndShareGpx(
        context: Context,
        title: String,
        startTime: Long,
        points: List<TrackPoint>
    ): Boolean {
        try {
            val exportsDir = File(context.cacheDir, "exports")
            if (!exportsDir.exists()) {
                exportsDir.mkdirs()
            }

            val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9_]"), "_").take(24)
            val fileName = "bikeroute_${sanitizedTitle}_${startTime}.gpx"
            val gpxFile = File(exportsDir, fileName)

            val gpxContent = buildGpxXml(title, startTime, points)
            FileWriter(gpxFile).use { writer ->
                writer.write(gpxContent)
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                gpxFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/gpx+xml"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "BikeRoute GPX: $title")
                putExtra(Intent.EXTRA_TEXT, "Here is my cycling route GPX file recorded with BikeRoute.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share GPX Route via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            return true
        } catch (e: Exception) {
            Log.e("GpxExporter", "Failed to export GPX: ${e.message}", e)
            return false
        }
    }

    fun buildGpxXml(title: String, startTime: Long, points: List<TrackPoint>): String {
        val sb = StringBuilder()
        sb.appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
        sb.appendLine("<gpx version=\"1.1\" creator=\"BikeRoute Android\" xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">")
        sb.appendLine("  <metadata>")
        sb.appendLine("    <name>${escapeXml(title)}</name>")
        sb.appendLine("    <time>${isoDateFormat.format(Date(startTime))}</time>")
        sb.appendLine("  </metadata>")
        sb.appendLine("  <trk>")
        sb.appendLine("    <name>${escapeXml(title)}</name>")
        sb.appendLine("    <type>Cycling</type>")
        sb.appendLine("    <trkseg>")

        for (pt in points) {
            val timeStr = isoDateFormat.format(Date(pt.timestamp))
            sb.appendLine(
                "      <trkpt lat=\"${pt.latitude}\" lon=\"${pt.longitude}\">" +
                        "<ele>${"%.1f".format(Locale.US, pt.altitude)}</ele>" +
                        "<time>$timeStr</time>" +
                        "</trkpt>"
            )
        }

        sb.appendLine("    </trkseg>")
        sb.appendLine("  </trk>")
        sb.appendLine("</gpx>")
        return sb.toString()
    }

    private fun escapeXml(input: String): String {
        return input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
