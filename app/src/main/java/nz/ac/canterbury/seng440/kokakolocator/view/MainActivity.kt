package nz.ac.canterbury.seng440.kokakolocator.view

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import nz.ac.canterbury.seng440.kokakolocator.R
import nz.ac.canterbury.seng440.kokakolocator.util.goTo

const val PREFS_KEY = "PREFS"
const val TOKEN_KEY = "TOKEN"
const val USERNAME_KEY = "USERNAME"
const val GROUP_NAME_KEY = "GROUP_NAME"
const val DEVICE_NAME_KEY = "DEVICE_NAME"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (isNotLoggedIn()) {
            goTo(LandingActivity::class)
        }

        findViewById<Button>(R.id.goToRecordButton).setOnClickListener { goTo(RecordAudioActivity::class) }
        findViewById<Button>(R.id.goToRecordingsButton).setOnClickListener { goTo(ViewRecordingsActivity::class) }
        findViewById<Button>(R.id.goToPrefsButton).setOnClickListener { goTo(PreferencesActivity::class) }

    }

    private fun isNotLoggedIn(): Boolean {
        val prefs = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        return !(with(prefs) { contains(TOKEN_KEY) && contains(DEVICE_NAME_KEY) })
    }

}
