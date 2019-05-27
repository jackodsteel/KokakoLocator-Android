package nz.ac.canterbury.seng440.kokakolocator.server

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class LoginRequestBody(
    val nameOrEmail: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class LoginResponseBody(
    val token: String
)