package nz.ac.canterbury.seng440.kokakolocator.server

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.squareup.moshi.JsonClass
import nz.ac.canterbury.seng440.kokakolocator.util.TAG
import nz.ac.canterbury.seng440.kokakolocator.util.responseBodyConverter
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.net.SocketTimeoutException

const val CACOPHONY_ROOT_URL = "https://c.jacksteel.co.nz/"
//const val CACOPHONY_ROOT_URL = "https://api-test.cacophony.org.nz/"

interface ICacophonyServer {
    fun login(username: String, password: String, onSuccess: (LoginResponseBody) -> Unit, onError: (String) -> Unit)
    fun register(username: String, email: String, password: String, onSuccess: (RegisterResponseBody) -> Unit, onError: (String) -> Unit)
    fun uploadRecording(
        token: String,
        audioFile: File,
        metadata: UploadAudioRequestMetadata,
        onSuccess: (UploadAudioResponseBody) -> Unit,
        onError: (String) -> Unit
    )

    fun uploadRecording(
        token: String,
        audioFileName: String,
        audioData: ByteArray,
        metadata: UploadAudioRequestMetadata,
        onSuccess: (UploadAudioResponseBody) -> Unit,
        onError: (String) -> Unit
    )
}

object CacophonyServer : ICacophonyServer {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(CACOPHONY_ROOT_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val cacophonyService = retrofit.create(CacophonyService::class.java)

    private val errorConverter = retrofit.responseBodyConverter<ErrorResponse>()

    override fun login(username: String, password: String, onSuccess: (LoginResponseBody) -> Unit, onError: (String) -> Unit) {
        val call = cacophonyService.login(LoginRequestBody(username, password))
        call.enqueue(GenericWebHandler<LoginResponseBody>(onSuccess, onError, errorConverter))
    }

    override fun register(username: String, email: String, password: String, onSuccess: (RegisterResponseBody) -> Unit, onError: (String) -> Unit) {
        val call = cacophonyService.register(RegisterRequestBody(username, email, password))
        call.enqueue(GenericWebHandler<RegisterResponseBody>(onSuccess, onError, errorConverter))
    }

    override fun uploadRecording(
        token: String,
        audioFile: File,
        metadata: UploadAudioRequestMetadata,
        onSuccess: (UploadAudioResponseBody) -> Unit,
        onError: (String) -> Unit
    ) {
        val filePart = MultipartBody.Part.createFormData(
            "file",
            audioFile.name,
            RequestBody.create(MediaType.parse("image/*"), audioFile)
        )
        val call = cacophonyService.uploadAudioRecording(token, filePart, metadata)
        call.enqueue(GenericWebHandler<UploadAudioResponseBody>(onSuccess, onError, errorConverter))
    }

    override fun uploadRecording(
        token: String,
        audioFileName: String,
        audioData: ByteArray,
        metadata: UploadAudioRequestMetadata,
        onSuccess: (UploadAudioResponseBody) -> Unit,
        onError: (String) -> Unit
    ) {
        val filePart = MultipartBody.Part.createFormData(
            "file",
            audioFileName,
            RequestBody.create(MediaType.parse("image/*"), audioData)
        )
        val call = cacophonyService.uploadAudioRecording(token, filePart, metadata)
        call.enqueue(GenericWebHandler<UploadAudioResponseBody>(onSuccess, onError, errorConverter))
    }

}

class GenericWebHandler<T>(
    private val onSuccess: (T) -> Unit,
    private val onError: (String) -> Unit,
    private val errorConverter: Converter<ResponseBody, ErrorResponse>
) : Callback<T> {
    override fun onResponse(call: Call<T>, response: Response<T>) {
        if (response.isSuccessful) {
            val responseBody = response.body()
            if (responseBody != null) {
                onSuccess(responseBody)
            } else {
                onError("Successful response but no body, had status: ${response.code()}, message: ${response.message()}")
            }
        } else {
            val errorBody = response.errorBody()
            if (errorBody != null) {
                val errorResponse = errorConverter.convert(errorBody).also { errorBody.close() }
                val message = errorResponse?.message?.replace("; ", " and ")
                    ?: "Unknown error with no body: ${response.code()}, message: ${response.message()}" //TODO string vars
                Log.w(TAG, "Error: $message")
                onError(message)
            } else {
                val message =
                    "Unknown error with no body: ${response.code()}, message: ${response.message()}" //TODO string vars
                Log.w(TAG, message)
                onError(message)
            }
        }
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        Log.e(TAG, "Unknown error when sending request", t)
        if (t is SocketTimeoutException) {
            onError("The connection to the server timed out!") //TODO use string var
        }
        onError("Unknown error when sending request: ${t.localizedMessage}; ${t.stackTrace}") //TODO use string var
    }
}

@JsonClass(generateAdapter = true)
data class ErrorResponse(
    val errorType: String?,
    val messages: List<String> = listOf(),
    val message: String?
)

@JsonClass(generateAdapter = true)
data class LoginRequestBody(
    val nameOrEmail: String,
    val password: String
)
@JsonClass(generateAdapter = true)
data class LoginResponseBody(
    val token: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequestBody(
    val username: String,
    val email: String,
    val password: String
)
@JsonClass(generateAdapter = true)
data class RegisterResponseBody(
    val token: String,
    val userData: Any?,
    val messages: List<String> = listOf()
)

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

interface CacophonyService {

    @POST("/authenticate_user")
    fun login(@Body body: LoginRequestBody): Call<LoginResponseBody>

    @POST("/api/v1/users")
    fun register(@Body body: RegisterRequestBody): Call<RegisterResponseBody>

    @Multipart
    @POST("/api/v1/recordings/testdev")
    fun uploadAudioRecording(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("data") data: UploadAudioRequestMetadata
    ): Call<UploadAudioResponseBody>

}