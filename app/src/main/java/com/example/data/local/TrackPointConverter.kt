package com.example.data.local

import com.example.model.TrackPoint
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object TrackPointConverter {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val listType = Types.newParameterizedType(List::class.java, TrackPoint::class.java)
    private val adapter = moshi.adapter<List<TrackPoint>>(listType)

    fun toJson(points: List<TrackPoint>): String {
        return try {
            adapter.toJson(points)
        } catch (e: Exception) {
            "[]"
        }
    }

    fun fromJson(json: String): List<TrackPoint> {
        return try {
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
