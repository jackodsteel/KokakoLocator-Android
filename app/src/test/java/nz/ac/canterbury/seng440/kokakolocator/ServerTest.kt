package nz.ac.canterbury.seng440.kokakolocator

import com.squareup.moshi.JsonDataException
import nz.ac.canterbury.seng440.kokakolocator.server.CacophonyServer
import nz.ac.canterbury.seng440.kokakolocator.server.LoginResponseBody
import nz.ac.canterbury.seng440.kokakolocator.util.responseBodyConverter
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ServerTest {

    companion object {
        const val TEST_JWT_TOKEN = "JWT 1234"
        val JSON_MEDIA_TYPE = MediaType.parse("application/json")
    }


    @Nested
    inner class Login {

        @Test
        fun `Response body without JWT throws error`() {
            // Given
            val converter = CacophonyServer.retrofit.responseBodyConverter<LoginResponseBody>()
            val responseBody = ResponseBody.create(JSON_MEDIA_TYPE, "{}")
            // When, Then
            assertThrows<JsonDataException> {
                converter.convert(responseBody)
            }
        }

        @Test
        fun `Response body with JWT returns LoginResponseBody`() {
            // Given
            val converter = CacophonyServer.retrofit.responseBodyConverter<LoginResponseBody>()
            val responseBody = ResponseBody.create(JSON_MEDIA_TYPE, "{\"token\": \"$TEST_JWT_TOKEN\"}")
            // When
            val converted = converter.convert(responseBody)
            // Then
            assertEquals(LoginResponseBody(token = TEST_JWT_TOKEN), converted)
        }

    }
}