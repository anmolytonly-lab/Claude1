package com.aicaller.app.data.local

import androidx.room.TypeConverter
import com.aicaller.app.data.local.entities.CallDirection
import com.aicaller.app.data.local.entities.SpamSource

class Converters {

    @TypeConverter
    fun toCallDirection(value: String): CallDirection = CallDirection.valueOf(value)

    @TypeConverter
    fun fromCallDirection(value: CallDirection): String = value.name

    @TypeConverter
    fun toSpamSource(value: String): SpamSource = SpamSource.valueOf(value)

    @TypeConverter
    fun fromSpamSource(value: SpamSource): String = value.name
}
