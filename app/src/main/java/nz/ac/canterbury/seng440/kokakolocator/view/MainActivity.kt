package nz.ac.canterbury.seng440.kokakolocator.view

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import nz.ac.canterbury.seng440.kokakolocator.R
import nz.ac.canterbury.seng440.kokakolocator.util.goTo
import nz.ac.canterbury.seng440.kokakolocator.util.prefs

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
        val prefs = prefs()
        return prefs.authToken == null || prefs.deviceName == null
    }

}
