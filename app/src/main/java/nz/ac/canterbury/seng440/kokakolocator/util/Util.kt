package nz.ac.canterbury.seng440.kokakolocator.util

import android.app.Activity
import android.content.Intent
import retrofit2.Retrofit
import kotlin.reflect.KClass

val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }

inline fun <reified T> Retrofit.responseBodyConverter(): retrofit2.Converter<okhttp3.ResponseBody, T> {
    return responseBodyConverter(T::class.java, T::class.java.annotations)
}

inline fun <T : Activity> Activity.goTo(clazz: KClass<T>) {
    startActivity(Intent(this, clazz.java))
}