package nz.ac.canterbury.seng440.kokakolocator.util

import retrofit2.Retrofit

val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }

inline fun <reified T> Retrofit.responseBodyConverter(): retrofit2.Converter<okhttp3.ResponseBody, T> {
    return responseBodyConverter(T::class.java, T::class.java.annotations)
}