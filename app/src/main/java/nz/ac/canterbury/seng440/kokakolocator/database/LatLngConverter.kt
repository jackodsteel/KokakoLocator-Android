package nz.ac.canterbury.seng440.kokakolocator.database

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng

object LatLngConverter {

    @JvmStatic
    @TypeConverter
    fun toLatLng(latLngStr: String): LatLng {
        return latLngStr.split(',').map { it.toDouble() }.let { LatLng(it[0], it[1]) }
    }

    @JvmStatic
    @TypeConverter
    fun fromLatLng(latLng: LatLng): String {
        return "${latLng.latitude},${latLng.longitude}"
    }
}