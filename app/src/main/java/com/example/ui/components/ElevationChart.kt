package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary

@Composable
fun ElevationChart(
    elevationProfile: List<Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = EmeraldPrimary,
    fillColorStart: Color = EmeraldPrimary.copy(alpha = 0.35f),
    fillColorEnd: Color = EmeraldPrimary.copy(alpha = 0.02f)
) {
    if (elevationProfile.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(16.dp)
        ) {
            Text(
                text = "Elevation profile not available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val minElev = elevationProfile.minOrNull() ?: 0.0
    val maxElev = elevationProfile.maxOrNull() ?: 100.0
    val elevRange = (maxElev - minElev).coerceAtLeast(10.0)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "ELEVATION PROFILE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            Text(
                text = "Max: ${maxElev.toInt()}m  Min: ${minElev.toInt()}m",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = CyanAccent
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .padding(top = 8.dp, bottom = 4.dp)
        ) {
            val width = size.width
            val height = size.height
            val stepX = width / (elevationProfile.size - 1)

            val linePath = Path()
            val fillPath = Path()

            elevationProfile.forEachIndexed { i, elev ->
                val normY = ((elev - minElev) / elevRange).toFloat()
                // Invert so higher elevation is nearer to the top
                val x = i * stepX
                val y = height - (normY * (height - 16f)) - 8f

                if (i == 0) {
                    linePath.moveTo(x, y)
                    fillPath.moveTo(x, height)
                    fillPath.lineTo(x, y)
                } else {
                    linePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            fillPath.lineTo(width, height)
            fillPath.close()

            // Draw gradient area under the curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(fillColorStart, fillColorEnd),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw subtle horizontal baseline
            drawLine(
                color = Color.White.copy(alpha = 0.1f),
                start = Offset(0f, height - 1f),
                end = Offset(width, height - 1f),
                strokeWidth = 1f
            )

            // Draw stroke line
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(
                    width = 4f,
                    cap = StrokeCap.Round
                )
            )
        }
    }
}
