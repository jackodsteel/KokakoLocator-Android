package nz.ac.canterbury.seng440.kokakolocator.util

import android.content.Context
import androidx.preference.PreferenceManager
import nz.ac.canterbury.seng440.kokakolocator.R

class Prefs(context: Context) {

    private val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val res = context.resources


    companion object {
        private const val TOKEN_KEY = "TOKEN"
        private const val USERNAME_KEY = "USERNAME"
        private const val GROUP_NAME_KEY = "GROUP_NAME"
        private const val DEVICE_NAME_KEY = "DEVICE_NAME"
    }

    var maxRecordLength: Int
        get() {
            return getInt(R.string.key_max_record_length, 0)
        }
        set(value) {
            setInt(R.string.key_max_record_length, value)
        }

    var authToken: String?
        get() {
            return getString(TOKEN_KEY)
        }
        set(value) {
            setString(TOKEN_KEY, value)
        }

    var username: String?
        get() {
            return getString(USERNAME_KEY)
        }
        set(value) {
            setString(USERNAME_KEY, value)
        }

    var groupName: String?
        get() {
            return getString(GROUP_NAME_KEY)
        }
        set(value) {
            setString(GROUP_NAME_KEY, value)
        }

    var deviceName: String?
        get() {
            return getString(DEVICE_NAME_KEY)
        }
        set(value) {
            setString(DEVICE_NAME_KEY, value)
        }


    fun setInt(key: Int, value: Int) {
        setInt(res.getString(key), value)
    }

    fun setInt(key: String, value: Int) {
        sharedPrefs.edit().putInt(key, value).apply()
    }

    fun getInt(key: String, default: Int): Int {
        return sharedPrefs.getInt(key, default)
    }

    fun getInt(stringKey: Int, default: Int): Int {
        return getInt(res.getString(stringKey), default)
    }

    fun setString(key: Int, value: String?) {
        setString(res.getString(key), value)
    }

    fun setString(key: String, value: String?) {
        sharedPrefs.edit().putString(key, value).apply()
    }

    fun getString(key: String, default: String? = null): String? {
        return sharedPrefs.getString(key, default)
    }

    fun getString(stringKey: Int, default: String? = null): String? {
        return getString(res.getString(stringKey), default)
    }


}

fun Context.prefs(): Prefs {
    return Prefs(this)
}