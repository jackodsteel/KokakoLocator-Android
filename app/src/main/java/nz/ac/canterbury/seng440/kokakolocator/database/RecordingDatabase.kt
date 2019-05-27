package nz.ac.canterbury.seng440.kokakolocator.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Recording::class], version = 5, exportSchema = false)
@TypeConverters(DateConverter::class, LatLngConverter::class)
abstract class RecordingDatabase : RoomDatabase() {
    abstract fun recordingDao(): RecordingDao

    companion object {
        private const val DATABASE_NAME = "recordings.db"

        @Volatile
        private var INSTANCE: RecordingDatabase? = null

        fun getAppDataBase(context: Context): RecordingDatabase {
            if (INSTANCE == null) {
                synchronized(RecordingDatabase::class) {
                    INSTANCE =
                        Room.databaseBuilder(context.applicationContext, RecordingDatabase::class.java, DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build()
                }
            }
            return INSTANCE!!
        }
    }
}

fun Context.database(): RecordingDatabase {
    return RecordingDatabase.getAppDataBase(this)
}