package nz.ac.canterbury.seng440.kokakolocator.server

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * Represents the direct calls that need to be made to the Cacophony API via Retrofit
 */
interface CacophonyApi {

    companion object {
        const val API_ROOT = "/api/v1"
    }

    @POST("/authenticate_user")
    fun login(@Body body: LoginRequestBody): Call<LoginResponseBody>

    @POST("$API_ROOT/users")
    fun register(@Body body: RegisterRequestBody): Call<RegisterResponseBody>

    @POST("$API_ROOT/devices")
    fun registerDevice(@Header("Authorization") auth: String, @Body body: RegisterDeviceRequestBody): Call<RegisterDeviceResponseBody>

    @POST("$API_ROOT/groups")
    fun registerGroup(@Header("Authorization") auth: String, @Body body: RegisterGroupRequestBody): Call<RegisterGroupResponseBody>

    @Multipart
    @POST("$API_ROOT/recordings/{deviceName}")
    fun uploadAudioRecording(
        @Header("Authorization") token: String,
        @Path(value = "deviceName", encoded = true) deviceName: String,
        @Part file: MultipartBody.Part,
        @Part("data") data: UploadAudioRequestMetadata
    ): Call<UploadAudioResponseBody>
}
