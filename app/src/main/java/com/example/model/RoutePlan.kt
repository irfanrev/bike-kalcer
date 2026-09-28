package com.example.model

data class RouteOption(
    val id: String,
    val name: String,
    val description: String,
    val distanceKm: Double,
    val durationMinutes: Int,
    val elevationGainMeters: Double,
    val elevationProfile: List<Double>,
    val difficulty: String,
    val coordinates: List<Pair<Double, Double>>, // List of (latitude, longitude)
    val isRecommendedFlat: Boolean = false
)

data class RoutePlanResult(
    val startAddress: String,
    val destinationAddress: String,
    val routes: List<RouteOption>,
    val selectedRouteIndex: Int = 0
)
