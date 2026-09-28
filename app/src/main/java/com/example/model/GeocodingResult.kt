package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NominatimPlace(
    @Json(name = "place_id") val placeId: Long = 0,
    val lat: String,
    val lon: String,
    @Json(name = "display_name") val displayName: String,
    val type: String? = null,
    val importance: Double? = 0.0
)
