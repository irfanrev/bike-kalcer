package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cycling_activities")
data class CyclingActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Long,
    val distanceMeters: Double,
    val avgSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val elevationGainMeters: Double,
    val caloriesBurned: Int,
    val trackPointsJson: String
)
