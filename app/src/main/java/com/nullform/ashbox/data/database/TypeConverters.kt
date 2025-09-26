package com.nullform.ashbox.data.database

import androidx.room.TypeConverter
import java.util.Date

class TypeConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromSenderType(value: SenderType): String {
        return value.name
    }

    @TypeConverter
    fun toSenderType(value: String): SenderType {
        return SenderType.valueOf(value)
    }
}
