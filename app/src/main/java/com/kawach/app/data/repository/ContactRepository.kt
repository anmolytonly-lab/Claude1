package com.kawach.app.data.repository

import com.kawach.app.data.local.dao.EmergencyContactDao
import com.kawach.app.data.local.entity.EmergencyContact
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val dao: EmergencyContactDao) {

    val personalContacts: Flow<List<EmergencyContact>> = dao.getPersonalContacts()
    val neighbourhoodContacts: Flow<List<EmergencyContact>> = dao.getNeighbourhoodContacts()
    val personalContactCount: Flow<Int> = dao.getPersonalContactCount()

    suspend fun add(contact: EmergencyContact) = dao.insert(contact)
    suspend fun update(contact: EmergencyContact) = dao.update(contact)
    suspend fun delete(contact: EmergencyContact) = dao.delete(contact)
}
