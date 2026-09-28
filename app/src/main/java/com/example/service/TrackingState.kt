package com.example.service

import com.example.model.TrackPoint

data class TrackingState(
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val startTime: Long = 0L,
    val elapsedSeconds: Long = 0L,
    val distanceMeters: Double = 0.0,
    val currentSpeedKmh: Double = 0.0,
    val avgSpeedKmh: Double = 0.0,
    val maxSpeedKmh: Double = 0.0,
    val elevationGainMeters: Double = 0.0,
    val currentAltitudeMeters: Double = 0.0,
    val currentPoint: TrackPoint? = null,
    val trackPoints: List<TrackPoint> = emptyList()
) {
    val distanceKm: Double
        get() = distanceMeters / 1000.0

    val formattedTime: String
        get() {
            val hours = elapsedSeconds / 3600
            val minutes = (elapsedSeconds % 3600) / 60
            val seconds = elapsedSeconds % 60
            return if (hours > 0) {
                "%02d:%02d:%02d".format(hours, minutes, seconds)
            } else {
                "%02d:%02d".format(minutes, seconds)
            }
        }

    val caloriesBurned: Int
        get() {
            // Standard cycling calorie burn estimate: ~30-35 kcal per km depending on effort
            return (distanceKm * 32.0 + (elevationGainMeters * 0.15)).toInt()
        }
}
