package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.HyperLime
import com.example.ui.theme.StravaOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun ElevationChart(
    elevationProfile: List<Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = HyperLime,
    glowColor: Color = CyberCyan.copy(alpha = 0.5f)
) {
    if (elevationProfile.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F172A).copy(alpha = 0.5f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Elevation topography unavailable",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
        return
    }

    val minElev = elevationProfile.minOrNull() ?: 0.0
    val maxElev = elevationProfile.maxOrNull() ?: 100.0
    val elevRange = (maxElev - minElev).coerceAtLeast(8.0)

    var touchIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(lineColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ELEVATION PROFILE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = TextSecondary,
                    letterSpacing = 1.2.sp
                )
            }

            if (touchIndex != null && touchIndex in elevationProfile.indices) {
                val current = elevationProfile[touchIndex!!]
                Text(
                    text = "${current.toInt()} m",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = HyperLime
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "MAX ${maxElev.toInt()}m",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = StravaOrange
                    )
                    Text(
                        text = "MIN ${minElev.toInt()}m",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                }
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .padding(top = 8.dp, bottom = 4.dp)
                .pointerInput(elevationProfile) {
                    detectTapGestures(
                        onPress = { offset ->
                            val totalWidth = this@pointerInput.size.width.toFloat()
                            val stepX = totalWidth / (elevationProfile.size - 1).coerceAtLeast(1)
                            val idx = (offset.x / stepX).toInt().coerceIn(0, elevationProfile.size - 1)
                            touchIndex = idx
                            tryAwaitRelease()
                            touchIndex = null
                        }
                    )
                }
                .pointerInput(elevationProfile) {
                    detectDragGestures(
                        onDragEnd = { touchIndex = null },
                        onDragCancel = { touchIndex = null },
                        onDrag = { change, _ ->
                            val totalWidth = this@pointerInput.size.width.toFloat()
                            val stepX = totalWidth / (elevationProfile.size - 1).coerceAtLeast(1)
                            val idx = (change.position.x / stepX).toInt().coerceIn(0, elevationProfile.size - 1)
                            touchIndex = idx
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            val stepX = width / (elevationProfile.size - 1)

            // Calculate points
            val points = elevationProfile.mapIndexed { i, elev ->
                val normY = ((elev - minElev) / elevRange).toFloat()
                val x = i * stepX
                val y = height - (normY * (height - 24f)) - 12f
                Offset(x, y)
            }

            // Draw smooth cubic curve
            val linePath = Path()
            val fillPath = Path()

            if (points.isNotEmpty()) {
                linePath.moveTo(points[0].x, points[0].y)
                fillPath.moveTo(points[0].x, height)
                fillPath.lineTo(points[0].x, points[0].y)

                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val cx = (p0.x + p1.x) / 2f
                    linePath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    fillPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                }

                fillPath.lineTo(points.last().x, height)
                fillPath.close()

                // Gradient area fill under curve
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.35f),
                            lineColor.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = height
                    )
                )

                // Neon glow line underneath
                drawPath(
                    path = linePath,
                    color = glowColor,
                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                )

                // Sharp neon line
                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                )

                // Peak Indicator
                val maxIdx = elevationProfile.indexOf(maxElev)
                if (maxIdx in points.indices) {
                    val peakPt = points[maxIdx]
                    drawCircle(Color.White, 5f, peakPt)
                    drawCircle(StravaOrange, 3.5f, peakPt)
                }

                // Touch scrubber cursor
                touchIndex?.let { idx ->
                    if (idx in points.indices) {
                        val pt = points[idx]
                        // Vertical guideline
                        drawLine(
                            color = Color.White.copy(alpha = 0.4f),
                            start = Offset(pt.x, 0f),
                            end = Offset(pt.x, height),
                            strokeWidth = 1.5f
                        )
                        // Active node circle
                        drawCircle(HyperLime, 7f, pt)
                        drawCircle(Color(0xFF0B0F17), 4f, pt)
                    }
                }
            }
        }
    }
}
