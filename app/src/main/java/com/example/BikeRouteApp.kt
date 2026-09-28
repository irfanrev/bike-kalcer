package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import org.osmdroid.config.Configuration

class BikeRouteApp : Application() {

    companion object {
        const val TRACKING_NOTIFICATION_CHANNEL_ID = "bikeroute_tracking_channel"
        lateinit var instance: BikeRouteApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize OSMDroid configuration with custom User-Agent to comply with OSM policy
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TRACKING_NOTIFICATION_CHANNEL_ID,
                "BikeRoute Live Ride Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live metrics and status during cycling activity"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
