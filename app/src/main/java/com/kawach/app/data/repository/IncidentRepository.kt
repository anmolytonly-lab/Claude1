package com.kawach.app.data.repository

import com.kawach.app.data.local.dao.IncidentLogDao
import com.kawach.app.data.local.entity.IncidentLogEntry
import kotlinx.coroutines.flow.Flow

class IncidentRepository(private val dao: IncidentLogDao) {

    val allEntries: Flow<List<IncidentLogEntry>> = dao.getAllEntries()

    suspend fun add(entry: IncidentLogEntry): Long = dao.insert(entry)
    suspend fun update(entry: IncidentLogEntry) = dao.update(entry)
    suspend fun delete(entry: IncidentLogEntry) = dao.delete(entry)
    suspend fun getById(id: Int): IncidentLogEntry? = dao.getById(id)
    suspend fun deleteAll() = dao.deleteAll()
}
