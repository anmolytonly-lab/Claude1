package com.aicaller.app.di

import android.content.Context
import androidx.room.Room
import com.aicaller.app.data.local.AppDatabase
import com.aicaller.app.data.local.dao.AutoReplyDao
import com.aicaller.app.data.local.dao.CallRecordDao
import com.aicaller.app.data.local.dao.ContactInsightDao
import com.aicaller.app.data.local.dao.SpamNumberDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideCallRecordDao(db: AppDatabase): CallRecordDao = db.callRecordDao()

    @Provides
    fun provideContactInsightDao(db: AppDatabase): ContactInsightDao = db.contactInsightDao()

    @Provides
    fun provideSpamNumberDao(db: AppDatabase): SpamNumberDao = db.spamNumberDao()

    @Provides
    fun provideAutoReplyDao(db: AppDatabase): AutoReplyDao = db.autoReplyDao()
}
