package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.CyclingActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CyclingDao {
    @Query("SELECT * FROM cycling_activities ORDER BY startTime DESC")
    fun getAllActivities(): Flow<List<CyclingActivityEntity>>

    @Query("SELECT * FROM cycling_activities WHERE id = :id")
    fun getActivityById(id: Long): Flow<CyclingActivityEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: CyclingActivityEntity): Long

    @Query("DELETE FROM cycling_activities WHERE id = :id")
    suspend fun deleteActivityById(id: Long)
}
