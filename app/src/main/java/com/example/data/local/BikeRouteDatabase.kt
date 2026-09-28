package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.CyclingActivityEntity

@Database(entities = [CyclingActivityEntity::class], version = 1, exportSchema = false)
abstract class BikeRouteDatabase : RoomDatabase() {
    abstract fun cyclingDao(): CyclingDao

    companion object {
        @Volatile
        private var INSTANCE: BikeRouteDatabase? = null

        fun getInstance(context: Context): BikeRouteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BikeRouteDatabase::class.java,
                    "bikeroute.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
