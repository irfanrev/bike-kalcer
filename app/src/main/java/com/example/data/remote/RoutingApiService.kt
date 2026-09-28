package com.example.data.remote

import com.example.model.NominatimPlace
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OsrmApiService {
    @GET("route/v1/biking/{coordinates}")
    suspend fun getBikingRoute(
        @Path("coordinates") coordinates: String,
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "geojson",
        @Query("alternatives") alternatives: Boolean = true,
        @Query("steps") steps: Boolean = true
    ): OsrmRouteResponse

    @GET("route/v1/driving/{coordinates}")
    suspend fun getDrivingRoute(
        @Path("coordinates") coordinates: String,
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "geojson",
        @Query("alternatives") alternatives: Boolean = true,
        @Query("steps") steps: Boolean = true
    ): OsrmRouteResponse
}

interface OpenElevationApiService {
    @POST("api/v1/lookup")
    suspend fun lookupElevation(
        @Body request: ElevationRequest
    ): ElevationResponse
}

interface NominatimApiService {
    @Headers("User-Agent: BikeRoute-Android-App/1.0 (contact: irfanmaulana.dev@gmail.com)")
    @GET("search")
    suspend fun searchPlaces(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 6,
        @Query("addressdetails") addressDetails: Int = 1
    ): List<NominatimPlace>

    @Headers("User-Agent: BikeRoute-Android-App/1.0 (contact: irfanmaulana.dev@gmail.com)")
    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "json"
    ): NominatimPlace
}
