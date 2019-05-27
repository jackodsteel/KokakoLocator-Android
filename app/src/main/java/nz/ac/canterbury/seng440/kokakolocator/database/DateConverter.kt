package nz.ac.canterbury.seng440.kokakolocator.database

import androidx.room.TypeConverter
import java.util.*


object DateConverter {

    @JvmStatic
    @TypeConverter
    fun toDate(dateLong: Long): Date {
        return Date(dateLong)
    }

    @JvmStatic
    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }
}