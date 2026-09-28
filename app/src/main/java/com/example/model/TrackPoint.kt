package com.example.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TrackPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val speedKmh: Float = 0f
)
