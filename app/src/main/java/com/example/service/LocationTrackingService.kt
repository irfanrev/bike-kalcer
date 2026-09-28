package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.BikeRouteApp
import com.example.MainActivity
import com.example.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LocationTrackingService : Service() {

    companion object {
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.service.ACTION_START"
        const val ACTION_PAUSE = "com.example.service.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.service.ACTION_RESUME"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"

        fun startTracking(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pauseTracking(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resumeTracking(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun stopTracking(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var stateObserverJob: Job? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var systemLocationManager: LocationManager? = null
    private var systemLocationListener: LocationListener? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        systemLocationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForegroundWithNotification()
                TrackingManager.startRide()
                startLocationUpdates()
                observeStateForNotification()
            }
            ACTION_PAUSE -> {
                TrackingManager.pauseRide()
                updateNotification(TrackingManager.trackingState.value)
            }
            ACTION_RESUME -> {
                TrackingManager.resumeRide()
                updateNotification(TrackingManager.trackingState.value)
            }
            ACTION_STOP -> {
                stopLocationUpdates()
                stateObserverJob?.cancel()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val initialNotification = buildNotification(TrackingManager.trackingState.value)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                initialNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }
    }

    private fun observeStateForNotification() {
        stateObserverJob?.cancel()
        stateObserverJob = serviceScope.launch {
            TrackingManager.trackingState.collectLatest { state ->
                if (state.isTracking) {
                    updateNotification(state)
                }
            }
        }
    }

    private fun updateNotification(state: TrackingState) {
        val notification = buildNotification(state)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
        manager?.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(state: TrackingState): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingOpenIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val status = if (state.isPaused) "Paused" else "Recording"
        val title = "BikeRoute: $status 🚴"
        val content = "%.2f km • %.1f km/h • %s".format(
            state.distanceKm,
            state.currentSpeedKmh,
            state.formattedTime
        )

        val builder = NotificationCompat.Builder(this, BikeRouteApp.TRACKING_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingOpenIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        // Add quick Pause / Resume action in notification
        if (state.isPaused) {
            val resumeIntent = Intent(this, LocationTrackingService::class.java).apply {
                action = ACTION_RESUME
            }
            val pendingResume = PendingIntent.getService(
                this, 1, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_media_play, "Resume", pendingResume)
        } else {
            val pauseIntent = Intent(this, LocationTrackingService::class.java).apply {
                action = ACTION_PAUSE
            }
            val pendingPause = PendingIntent.getService(
                this, 2, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_media_pause, "Pause", pendingPause)
        }

        return builder.build()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateIntervalMillis(1000L)
            .setMinUpdateDistanceMeters(1.0f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (loc in result.locations) {
                    TrackingManager.onNewLocation(loc)
                }
            }
        }

        try {
            locationCallback?.let {
                fusedLocationClient.requestLocationUpdates(locationRequest, it, Looper.getMainLooper())
            }
        } catch (e: Exception) {
            Log.e("TrackingService", "FusedLocation error, falling back to system provider: ${e.message}")
            fallbackToSystemLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun fallbackToSystemLocation() {
        try {
            systemLocationListener = LocationListener { location ->
                TrackingManager.onNewLocation(location)
            }
            systemLocationListener?.let { listener ->
                if (systemLocationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
                    systemLocationManager?.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        2000L,
                        1.0f,
                        listener,
                        Looper.getMainLooper()
                    )
                } else if (systemLocationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true) {
                    systemLocationManager?.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        2000L,
                        1.0f,
                        listener,
                        Looper.getMainLooper()
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("TrackingService", "System LocationManager fallback failed: ${e.message}")
        }
    }

    private fun stopLocationUpdates() {
        try {
            locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
            systemLocationListener?.let { systemLocationManager?.removeUpdates(it) }
        } catch (e: Exception) {
            Log.e("TrackingService", "Error stopping location updates: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        stateObserverJob?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
