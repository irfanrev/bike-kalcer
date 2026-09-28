package com.example.data.repository

import com.example.data.local.CyclingDao
import com.example.model.CyclingActivityEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class CyclingRepository(private val cyclingDao: CyclingDao) {

    val allActivities: Flow<List<CyclingActivityEntity>> =
        cyclingDao.getAllActivities().flowOn(Dispatchers.IO)

    fun getActivityById(id: Long): Flow<CyclingActivityEntity?> =
        cyclingDao.getActivityById(id).flowOn(Dispatchers.IO)

    suspend fun saveActivity(activity: CyclingActivityEntity): Long = withContext(Dispatchers.IO) {
        cyclingDao.insertActivity(activity)
    }

    suspend fun deleteActivity(id: Long) = withContext(Dispatchers.IO) {
        cyclingDao.deleteActivityById(id)
    }
}
