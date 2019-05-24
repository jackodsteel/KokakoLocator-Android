package nz.ac.canterbury.seng440.kokakolocator.server

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
import nz.ac.canterbury.seng440.kokakolocator.util.TAG
import nz.ac.canterbury.seng440.kokakolocator.util.responseBodyConverter
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.net.SocketTimeoutException

const val CACOPHONY_ROOT_URL = "https://c.jacksteel.co.nz/"
//const val CACOPHONY_ROOT_URL = "https://api-test.cacophony.org.nz/"

interface ICacophonyServer {
    fun login(username: String, password: String, onSuccess: (LoginResponseBody) -> Unit, onError: (String) -> Unit)

    /**
     * Register the user, also create them a default group and device to use
     */
    fun register(username: String, email: String, password: String, onSuccess: (RegisterResponseBody) -> Unit, onError: (String) -> Unit)
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
                        registerResponse
                    )
                }, onError, errorConverter))
            }, onError, errorConverter))
        }, onError, errorConverter))

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
                val errorResponse = try {
                    errorConverter.convert(errorBody).also { errorBody.close() }
                } catch (e: JsonDataException) {
                    val message =
                        "Unknown error with no body: ${response.code()}, message: ${response.message()}" //TODO string vars
                    Log.w(TAG, message)
                    onError(message)
                    return
                }
                Log.w(TAG, errorResponse?.message)
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
data class RegisterDeviceRequestBody(
    val devicename: String,
    val password: String,
    val group: String
)

@JsonClass(generateAdapter = true)
data class RegisterDeviceResponseBody(
    val token: String,
    val messages: List<String> = listOf()
)

@JsonClass(generateAdapter = true)
data class RegisterGroupRequestBody(
    val groupname: String
)

@JsonClass(generateAdapter = true)
data class RegisterGroupResponseBody(
    val messages: List<String> = listOf()
)

interface CacophonyService {

    @POST("/authenticate_user")
    fun login(@Body body: LoginRequestBody): Call<LoginResponseBody>

    @POST("/api/v1/users")
    fun register(@Body body: RegisterRequestBody): Call<RegisterResponseBody>

    @POST("/api/v1/devices")
    fun registerDevice(@Header("Authorization") auth: String, @Body body: RegisterDeviceRequestBody): Call<RegisterDeviceResponseBody>

    @POST("/api/v1/groups")
    fun registerGroup(@Header("Authorization") auth: String, @Body body: RegisterGroupRequestBody): Call<RegisterGroupResponseBody>

}

/**
 * Test code only
 */
fun main() {
//    CacophonyServer.login("jacksteel", "test_password", { println(it)}, { println(it)})
//    CacophonyServer.register("jacksteel25", "jacksteel@test.test","12345678", { println(it)}, { println(it)})
}