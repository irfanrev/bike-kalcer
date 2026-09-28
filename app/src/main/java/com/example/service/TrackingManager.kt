package com.example.service

import android.location.Location
import com.example.model.TrackPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object TrackingManager {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var timerJob: Job? = null

    private val _trackingState = MutableStateFlow(TrackingState())
    val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

    private var lastLocation: Location? = null

    fun startRide() {
        lastLocation = null
        val now = System.currentTimeMillis()
        _trackingState.value = TrackingState(
            isTracking = true,
            isPaused = false,
            startTime = now,
            elapsedSeconds = 0L,
            distanceMeters = 0.0,
            trackPoints = emptyList()
        )
        startTimer()
    }

    fun pauseRide() {
        if (!_trackingState.value.isTracking) return
        _trackingState.update { it.copy(isPaused = true, currentSpeedKmh = 0.0) }
        timerJob?.cancel()
    }

    fun resumeRide() {
        if (!_trackingState.value.isTracking) return
        _trackingState.update { it.copy(isPaused = false) }
        startTimer()
    }

    fun stopRide(): TrackingState {
        timerJob?.cancel()
        val finalState = _trackingState.value
        _trackingState.value = TrackingState()
        lastLocation = null
        return finalState
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                delay(1000L)
                if (!_trackingState.value.isPaused && _trackingState.value.isTracking) {
                    _trackingState.update { state ->
                        val newSeconds = state.elapsedSeconds + 1
                        val hours = newSeconds / 3600.0
                        val avgSpeed = if (hours > 0) (state.distanceMeters / 1000.0) / hours else 0.0
                        state.copy(
                            elapsedSeconds = newSeconds,
                            avgSpeedKmh = (avgSpeed * 10).toInt() / 10.0
                        )
                    }
                }
            }
        }
    }

    fun onNewLocation(location: Location) {
        val currentState = _trackingState.value
        if (!currentState.isTracking || currentState.isPaused) return

        val speedKmh = if (location.hasSpeed()) {
            (location.speed * 3.6).coerceAtLeast(0.0)
        } else {
            0.0
        }

        var addedDistance = 0.0
        var elevationDiff = 0.0

        val prev = lastLocation
        if (prev != null) {
            val dist = prev.distanceTo(location).toDouble()
            // Filter out GPS teleport anomalies (> 45 m/s or 160 km/h)
            if (dist in 1.0..300.0) {
                addedDistance = dist
            }

            // Elevation smoothing: only count positive altitude changes >= 1.5 meters to filter GPS noise
            if (location.hasAltitude() && prev.hasAltitude()) {
                val altDiff = location.altitude - prev.altitude
                if (altDiff >= 1.5) {
                    elevationDiff = altDiff
                }
            }
        }

        lastLocation = location

        val newPoint = TrackPoint(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = if (location.hasAltitude()) location.altitude else 0.0,
            timestamp = System.currentTimeMillis(),
            speedKmh = speedKmh.toFloat()
        )

        _trackingState.update { state ->
            val updatedDistance = state.distanceMeters + addedDistance
            val updatedElevGain = state.elevationGainMeters + elevationDiff
            val updatedMaxSpeed = maxOf(state.maxSpeedKmh, speedKmh)
            val updatedPoints = state.trackPoints + newPoint

            state.copy(
                distanceMeters = updatedDistance,
                currentSpeedKmh = (speedKmh * 10).toInt() / 10.0,
                maxSpeedKmh = (updatedMaxSpeed * 10).toInt() / 10.0,
                elevationGainMeters = (updatedElevGain * 10).toInt() / 10.0,
                currentAltitudeMeters = if (location.hasAltitude()) location.altitude else state.currentAltitudeMeters,
                currentPoint = newPoint,
                trackPoints = updatedPoints
            )
        }
    }
}
