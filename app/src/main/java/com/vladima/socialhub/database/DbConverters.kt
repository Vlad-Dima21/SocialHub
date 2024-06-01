package com.vladima.socialhub.database

import androidx.room.TypeConverter
import java.util.Date

public class DbConverters {
    @TypeConverter
    public fun fromDateToLong(date: Date): Long = date.time
    @TypeConverter
    public fun fromLongToDate(long: Long): Date = Date(long)
}