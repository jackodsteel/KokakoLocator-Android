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
 * Represents the direct calls that need to be made to the Cacophony API
 */
interface CacophonyApi {

    @POST("/authenticate_user")
    fun login(@Body body: LoginRequestBody): Call<LoginResponseBody>

    @POST("/api/v1/users")
    fun register(@Body body: RegisterRequestBody): Call<RegisterResponseBody>

    @POST("/api/v1/devices")
    fun registerDevice(@Header("Authorization") auth: String, @Body body: RegisterDeviceRequestBody): Call<RegisterDeviceResponseBody>

    @POST("/api/v1/groups")
    fun registerGroup(@Header("Authorization") auth: String, @Body body: RegisterGroupRequestBody): Call<RegisterGroupResponseBody>

    @Multipart
    @POST("/api/v1/recordings/{deviceName}")
    fun uploadAudioRecording(
        @Header("Authorization") token: String,
        @Path(value = "deviceName", encoded = true) deviceName: String,
        @Part file: MultipartBody.Part,
        @Part("data") data: UploadAudioRequestMetadata
    ): Call<UploadAudioResponseBody>
}
