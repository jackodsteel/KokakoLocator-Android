package nz.ac.canterbury.seng440.kokakolocator.server

import android.util.Log
import nz.ac.canterbury.seng440.kokakolocator.util.TAG
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

const val CACOPHONY_ROOT_URL = "https://c.jacksteel.co.nz/"
//const val CACOPHONY_ROOT_URL = "https://api-test.cacophony.org.nz/"

interface ICacophonyServer {
    fun login(username: String, password: String, onSuccess: (LoginResponseBody) -> Unit, onError: (String) -> Unit)
    fun register(username: String, email: String, password: String, onSuccess: (RegisterResponseBody) -> Unit, onError: (String) -> Unit)
}

object CacophonyServer : ICacophonyServer {

    private val retrofit = Retrofit.Builder()
        .baseUrl(CACOPHONY_ROOT_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val cacophonyService = retrofit.create(CacophonyService::class.java)

    private val errorConverter = retrofit.responseBodyConverter<ErrorResponse>(ErrorResponse::class.java, ErrorResponse::class.java.annotations)

    override fun login(username: String, password: String, onSuccess: (LoginResponseBody) -> Unit, onError: (String) -> Unit) {
        val call = cacophonyService.login(LoginRequestBody(username, password))
        call.enqueue(GenericWebHandler<LoginResponseBody>(onSuccess, onError, errorConverter))
    }

    override fun register(username: String, email: String, password: String, onSuccess: (RegisterResponseBody) -> Unit, onError: (String) -> Unit) {
        val call = cacophonyService.register(RegisterRequestBody(username, email, password))
        call.enqueue(GenericWebHandler<RegisterResponseBody>(onSuccess, onError, errorConverter))
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
                    ?: "Unknown error with no body: ${response.code()}, message: ${response.message()}"
                Log.w(TAG, "Error: $message")
                onError(message)
            } else {
                val message = "Unknown error with no body: ${response.code()}, message: ${response.message()}"
                Log.w(TAG, message)
                onError(message)
            }
        }
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        Log.e(TAG, "Unknown error when sending request", t)
        onError("Unknown error when sending request: ${t.localizedMessage}; ${t.stackTrace}")
    }
}

data class ErrorResponse(
    val errorType: String?,
    val messages: List<String>?,
    val message: String?
)

data class LoginRequestBody(
    val nameOrEmail: String,
    val password: String
)
data class LoginResponseBody(
    val token: String
)

data class RegisterRequestBody(
    val username: String,
    val email: String,
    val password: String
)

data class RegisterResponseBody(
    val token: String?,
    val userData: Any?,
    val messages: List<String>?
)

interface CacophonyService {

    @POST("/authenticate_user")
    fun login(@Body body: LoginRequestBody): Call<LoginResponseBody>

    @POST("/api/v1/users")
    fun register(@Body body: RegisterRequestBody): Call<RegisterResponseBody>

}

/**
 * Test code only
 */
fun main() {
//    CacophonyServer.login("jacksteel", "test_password", { println(it)}, { println(it)})
//    CacophonyServer.register("jacksteel25", "jacksteel@test.test","12345678", { println(it)}, { println(it)})
}