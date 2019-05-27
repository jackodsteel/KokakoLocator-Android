package nz.ac.canterbury.seng440.kokakolocator.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface RecordingDao {

    @Insert
    fun insert(recording: Recording): Long

    @Update
    fun update(recording: Recording)

    @Delete
    fun delete(recording: Recording)

    @Query("SELECT * FROM ${Recording.TABLE_NAME}")
    fun getAll(): List<Recording>

    @Query("SELECT * FROM ${Recording.TABLE_NAME} WHERE id=:id")
    fun getById(id: Long): Recording?

}