package nz.ac.canterbury.seng440.kokakolocator.server

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UploadAudioRequestMetadata(
    val type: String = "audio",
    val duration: String? = null,
    val recordingDataTime: String? = null,
    val location: String? = null,
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
