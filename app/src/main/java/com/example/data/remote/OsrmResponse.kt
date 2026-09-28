package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OsrmRouteResponse(
    val code: String,
    val routes: List<OsrmRoute>? = null
)

@JsonClass(generateAdapter = true)
data class OsrmRoute(
    val distance: Double, // in meters
    val duration: Double, // in seconds
    val geometry: OsrmGeometry? = null,
    val legs: List<OsrmLeg>? = null
)

@JsonClass(generateAdapter = true)
data class OsrmGeometry(
    val coordinates: List<List<Double>>, // [longitude, latitude]
    val type: String
)

@JsonClass(generateAdapter = true)
data class OsrmLeg(
    val summary: String? = null,
    val steps: List<OsrmStep>? = null
)

@JsonClass(generateAdapter = true)
data class OsrmStep(
    val name: String? = null,
    val distance: Double = 0.0,
    val duration: Double = 0.0
)
