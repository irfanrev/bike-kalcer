package com.example.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ElevationLocation(
    val latitude: Double,
    val longitude: Double
)

@JsonClass(generateAdapter = true)
data class ElevationRequest(
    val locations: List<ElevationLocation>
)

@JsonClass(generateAdapter = true)
data class ElevationResult(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double
)

@JsonClass(generateAdapter = true)
data class ElevationResponse(
    val results: List<ElevationResult>
)
