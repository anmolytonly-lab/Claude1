package com.aicaller.app.data.repository

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import com.aicaller.app.data.local.dao.ContactInsightDao
import com.aicaller.app.data.local.entities.ContactInsightEntity
import com.aicaller.app.util.PhoneNumberUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val insightDao: ContactInsightDao
) {

    /** Reads the device address book. Requires READ_CONTACTS. */
    suspend fun getDeviceContacts(): List<Contact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<Contact>()
        val resolver: ContentResolver = context.contentResolver
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI
            ),
            null, null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )

        cursor?.use {
            val idIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
            while (it.moveToNext()) {
                contacts += Contact(
                    id = it.getString(idIdx) ?: "",
                    name = it.getString(nameIdx) ?: "Unknown",
                    phoneNumber = it.getString(numberIdx) ?: "",
                    photoUri = if (photoIdx >= 0) it.getString(photoIdx) else null
                )
            }
        }
        contacts.distinctBy { PhoneNumberUtils.normalize(it.phoneNumber) }
    }

    suspend fun lookupNameByNumber(phoneNumber: String): String? {
        val normalized = PhoneNumberUtils.normalize(phoneNumber)
        return getDeviceContacts().firstOrNull { PhoneNumberUtils.normalize(it.phoneNumber) == normalized }?.name
    }

    fun observeInsights(): Flow<List<ContactInsightEntity>> = insightDao.observeAll()

    fun observeInsight(phoneNumber: String): Flow<ContactInsightEntity?> =
        insightDao.observeByNumber(PhoneNumberUtils.normalize(phoneNumber))

    suspend fun upsertInsight(insight: ContactInsightEntity) =
        insightDao.upsert(insight.copy(phoneNumber = PhoneNumberUtils.normalize(insight.phoneNumber)))

    suspend fun getInsight(phoneNumber: String): ContactInsightEntity? =
        insightDao.getByNumber(PhoneNumberUtils.normalize(phoneNumber))
}
