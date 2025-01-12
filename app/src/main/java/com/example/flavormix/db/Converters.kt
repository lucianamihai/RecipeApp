package com.example.flavormix.db

import androidx.room.TypeConverter
import java.util.Date

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromString(value: String?): Any? {
        return value
    }

    @TypeConverter
    fun anyToString(any: Any?): String? {
        return any?.toString()
    }
}
