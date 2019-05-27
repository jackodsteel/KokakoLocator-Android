package nz.ac.canterbury.seng440.kokakolocator.server

import com.squareup.moshi.JsonClass


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

data class SuccessfulRegistrationData(
    val token: String,
    val username: String,
    val groupName: String,
    val deviceName: String
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