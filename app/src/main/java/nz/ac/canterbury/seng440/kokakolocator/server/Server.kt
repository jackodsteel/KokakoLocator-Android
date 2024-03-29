package nz.ac.canterbury.seng440.kokakolocator.server

import android.content.Context
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import nz.ac.canterbury.seng440.kokakolocator.R
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
import java.io.File
import java.net.SocketTimeoutException
import java.util.*

//const val CACOPHONY_ROOT_URL = "https://c.jacksteel.co.nz/"
const val CACOPHONY_ROOT_URL = "https://api-test.cacophony.org.nz/"

/**
 * Wraps the CacophonyApi in a more usable interface for internal use
 */
interface CacophonyServer {
    fun login(
        username: String,
        password: String,
        onSuccess: (LoginResponseBody) -> Unit,
        onError: (String) -> Unit
    )

    /**
     * Register the user, also create them a default group and device to use
     */
    fun register(
        username: String,
        email: String,
        password: String,
        onSuccess: (SuccessfulRegistrationData) -> Unit,
        onError: (String) -> Unit
    )

    fun uploadRecording(
        token: String,
        deviceName: String,
        audioFile: File,
        metadata: UploadAudioRequestMetadata,
        onSuccess: (UploadAudioResponseBody) -> Unit,
        onError: (String) -> Unit
    )

    fun uploadRecording(
        token: String,
        deviceName: String,
        audioFileName: String,
        audioData: ByteArray,
        metadata: UploadAudioRequestMetadata,
        onSuccess: (UploadAudioResponseBody) -> Unit,
        onError: (String) -> Unit
    )

    companion object {

        @Volatile
        private var INSTANCE: CacophonyServer? = null

        fun getServer(context: Context): CacophonyServer {
            if (INSTANCE == null) {
                synchronized(CacophonyServer::class) {
                    INSTANCE = CacophonyServerImpl(context)
                }
            }
            return INSTANCE!!
        }
    }
}

fun Context.cacophonyServer(): CacophonyServer {
    return CacophonyServer.getServer(this)
}

class CacophonyServerImpl(private val context: Context) : CacophonyServer {

    private val moshi: Moshi = Moshi.Builder()
        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
        .add(LatLngAdapter())
        .build()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(CACOPHONY_ROOT_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val cacophonyService = retrofit.create(CacophonyApi::class.java)

    private val errorConverter = retrofit.responseBodyConverter<ErrorResponse>()

    override fun login(
        username: String,
        password: String,
        onSuccess: (LoginResponseBody) -> Unit,
        onError: (String) -> Unit
    ) {
        val call = cacophonyService.login(LoginRequestBody(username, password))
        call.enqueue(GenericWebHandler<LoginResponseBody>(onSuccess, onError, errorConverter, context))
    }

    override fun register(
        username: String,
        email: String,
        password: String,
        onSuccess: (SuccessfulRegistrationData) -> Unit,
        onError: (String) -> Unit
    ) {
        val defaultGroupName = "${username}_default"
        val defaultDeviceName = "${username}_default_device"
        val registerUserCall = cacophonyService.register(RegisterRequestBody(username, email, password))
        registerUserCall.enqueue(GenericWebHandler<RegisterResponseBody>({ registerResponse ->
            val registerDefaultGroupCall =
                cacophonyService.registerGroup(registerResponse.token, RegisterGroupRequestBody(defaultGroupName))
            registerDefaultGroupCall.enqueue(GenericWebHandler<RegisterGroupResponseBody>({
                val registerDefaultDeviceCall = cacophonyService.registerDevice(
                    registerResponse.token,
                    RegisterDeviceRequestBody(defaultDeviceName, password, defaultGroupName)
                )
                registerDefaultDeviceCall.enqueue(GenericWebHandler<RegisterDeviceResponseBody>({
                    onSuccess.invoke(
                        SuccessfulRegistrationData(
                            registerResponse.token,
                            username,
                            defaultGroupName,
                            defaultDeviceName
                        )
                    )
                }, onError, errorConverter, context))
            }, onError, errorConverter, context))
        }, onError, errorConverter, context))

    }

    override fun uploadRecording(
        token: String,
        deviceName: String,
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
        Log.i(TAG, moshi.adapter<UploadAudioRequestMetadata>(UploadAudioRequestMetadata::class.java).toJson(metadata))
        val call = cacophonyService.uploadAudioRecording(token, deviceName, filePart, metadata)
        call.enqueue(GenericWebHandler<UploadAudioResponseBody>(onSuccess, onError, errorConverter, context))
    }

    override fun uploadRecording(
        token: String,
        deviceName: String,
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
        Log.i(TAG, moshi.adapter<UploadAudioRequestMetadata>(UploadAudioRequestMetadata::class.java).toJson(metadata))
        val call = cacophonyService.uploadAudioRecording(token, deviceName, filePart, metadata)
        call.enqueue(GenericWebHandler<UploadAudioResponseBody>(onSuccess, onError, errorConverter, context))
    }

}

/**
 * Wraps a Retrofit Callback and instead either calls the onSuccess function with the expected response,
 * or calls onError with a nicely formatted string.
 */
class GenericWebHandler<T>(
    private val onSuccess: (T) -> Unit,
    private val onError: (String) -> Unit,
    private val errorConverter: Converter<ResponseBody, ErrorResponse>,
    private val context: Context
) : Callback<T> {
    override fun onResponse(call: Call<T>, response: Response<T>) {
        if (response.isSuccessful) {
            val responseBody = response.body()
            if (responseBody != null) {
                onSuccess(responseBody)
            } else {
                onError(context.getString(R.string.successful_response_no_body, response.code(), response.message()))
            }
        } else {
            val errorBody = response.errorBody()
            if (errorBody != null) {
                val errorResponse = try {
                    errorConverter.convert(errorBody).also { errorBody.close() }
                } catch (e: JsonDataException) {
                    val message =
                        context.getString(R.string.unknown_error_no_body, response.code(), response.message())
                    Log.w(TAG, message)
                    onError(message)
                    return
                }
                val message = errorResponse?.message?.replace("; ", " ${context.getString(R.string.and)} ")
                    ?: context.getString(R.string.unknown_error_no_body, response.code(), response.message())
                Log.w(TAG, "Error: $message")
                onError(message)
            } else {
                val message =
                    context.getString(R.string.unknown_error_no_body, response.code(), response.message())
                Log.w(TAG, message)
                onError(message)
            }
        }
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        if (t is SocketTimeoutException) {
            onError(context.getString(R.string.server_timeout))
        } else {
            Log.e(TAG, "Unknown error when sending request", t)
            onError(context.getString(R.string.unknown_error_exception, t.localizedMessage))
        }
    }
}

@JsonClass(generateAdapter = true)
data class ErrorResponse(
    val errorType: String?,
    val messages: List<String> = listOf(),
    val message: String?
)
