package nz.ac.canterbury.seng440.kokakolocator.server

import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class UploadAudioRequestMetadata(
    val type: String = "audio",
    val duration: Long? = null,
    val recordingDateTime: Date = Calendar.getInstance().time,
    val location: LatLng? = null,
    val version: String? = null,
    val batteryCharging: Boolean? = null,
    val batteryLevel: String? = null,
    val airplaneModeOn: Boolean? = null,
    val additionalMetadata: List<String>? = null,
    val comment: String? = null
)

@JsonClass(generateAdapter = true)
data class UploadAudioResponseBody(
    val recordingId: String,
    val messages: List<String> = listOf()
)
