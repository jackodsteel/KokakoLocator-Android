package nz.ac.canterbury.seng440.kokakolocator.server

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST


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
        call.enqueue(object : Callback<LoginResponseBody> {
            override fun onResponse(call: Call<LoginResponseBody>, response: Response<LoginResponseBody>) {
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
                        onError(errorResponse.toString())
                    } else {
                        onError("Unknown error with no body: ${response.code()}, message: ${response.message()}")
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponseBody>, t: Throwable) {
                onError("Unknown error when sending request: ${t.localizedMessage}")
            }

        })
    }


    override fun register(username: String, email: String, password: String, onSuccess: (RegisterResponseBody) -> Unit, onError: (String) -> Unit) {
        val call = cacophonyService.register(RegisterRequestBody(username, email, password))
        call.enqueue(object : Callback<RegisterResponseBody> {
            override fun onResponse(call: Call<RegisterResponseBody>, response: Response<RegisterResponseBody>) {
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
                        onError(errorResponse?.message?.replace("; ", " and ") ?: "Unknown error with no body: ${response.code()}, message: ${response.message()}")
                    } else {
                        onError("Unknown error with no body: ${response.code()}, message: ${response.message()}")
                    }
                }
            }

            override fun onFailure(call: Call<RegisterResponseBody>, t: Throwable) {
                onError("Unknown error when sending request: ${t.localizedMessage}; ${t.stackTrace}")
            }

        })
    }

}

const val CACOPHONY_ROOT_URL = "https://api-test.cacophony.org.nz/"

data class ErrorResponse(
    val errorType: String?,
    val messages: List<String>?,
    val message: String?
)

data class LoginRequestBody(
    private val nameOrEmail: String,
    private val password: String
)

data class LoginResponseBody(
    val success: Boolean
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