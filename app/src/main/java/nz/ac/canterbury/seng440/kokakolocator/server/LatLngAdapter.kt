package nz.ac.canterbury.seng440.kokakolocator.server

import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

/**
 * Convert the LatLng object to and from a two item array, as expected by CacophonyApi
 */
class LatLngAdapter {
    @FromJson
    fun fromJson(latLngArr: Array<Double>): LatLng {
        return LatLng(latLngArr[0], latLngArr[1])
    }

    @ToJson
    fun toJson(latLng: LatLng): Array<Double> {
        return arrayOf(latLng.latitude, latLng.longitude)
    }
}