package nz.ac.canterbury.seng440.kokakolocator.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = Recording.TABLE_NAME)
class Recording(
    val fileName: String,
    val latLong: String,
    val dateTime: String,
    @PrimaryKey(autoGenerate = true) var id: Long = 0
) {
    companion object {
        const val TABLE_NAME = "recordings"
    }

    override fun toString(): String {
        return "Recording(id: $id, fileName: $fileName)"
    }
}