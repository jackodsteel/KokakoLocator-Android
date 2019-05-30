package nz.ac.canterbury.seng440.kokakolocator.util

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import retrofit2.Retrofit
import kotlin.reflect.KClass

/**
 * Return the current classes simple class name for log tagging.
 */
val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }

/**
 * Wraps the standard Retrofit responseBodyConverter to allow Kotlin type definitions.
 * @see retrofit2.Retrofit.responseBodyConverter
 */
inline fun <reified T> Retrofit.responseBodyConverter(): retrofit2.Converter<okhttp3.ResponseBody, T> {
    return responseBodyConverter(T::class.java, T::class.java.annotations)
}

/**
 * Create an explicit intent to the given Activity class and start it using the calling's Activity context.
 */
fun <T : Activity> Activity.goTo(clazz: KClass<T>) {
    startActivity(Intent(this, clazz.java))
}

/**
 * Create a Toast and show it using the current Activity context.
 */
fun Activity.toast(message: String, length: Int) {
    Toast.makeText(
        this,
        message,
        length
    ).show()
}

/**
 * Create a long Toast and show it using the current Activity context.
 */
fun Activity.longToast(message: String) {
    toast(message, Toast.LENGTH_LONG)
}

/**
 * Create a short Toast and show it using the current Activity context.
 */
fun Activity.shortToast(message: String) {
    toast(message, Toast.LENGTH_SHORT)
}