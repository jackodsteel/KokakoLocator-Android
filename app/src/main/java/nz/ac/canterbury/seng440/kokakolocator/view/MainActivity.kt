package nz.ac.canterbury.seng440.kokakolocator.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng440.kokakolocator.R
import nz.ac.canterbury.seng440.kokakolocator.database.Recording
import nz.ac.canterbury.seng440.kokakolocator.database.database
import nz.ac.canterbury.seng440.kokakolocator.server.CacophonyServer
import nz.ac.canterbury.seng440.kokakolocator.server.UploadAudioRequestMetadata
import nz.ac.canterbury.seng440.kokakolocator.util.TAG
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
        findViewById<Button>(R.id.goToRecordButton).setOnClickListener { goTo(LandingActivity::class) } //TODO remove this when not debugging
        findViewById<Button>(R.id.goToRecordingsButton).setOnClickListener { goTo(ViewRecordingsActivity::class) }
        findViewById<Button>(R.id.goToPrefsButton).setOnClickListener { goTo(PreferencesActivity::class) }
        findViewById<Button>(R.id.goToPrefsButton).setOnClickListener { uploadAudioRecording() } // TODO remove this when not debugging

    }

    private fun isNotLoggedIn(): Boolean {
        val prefs = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        return !(with(prefs) { contains(TOKEN_KEY) && contains(DEVICE_NAME_KEY) })
    }


    private fun uploadAudioRecording() {

        val token: String? = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE).getString(
            TOKEN_KEY, null
        )
        val deviceName: String? = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE).getString(
            DEVICE_NAME_KEY, null
        )

        if (token == null || deviceName == null) {
            Log.i(TAG, "token: $token, deviceName: $deviceName")
            return //TODO handle dis
        }

        val file = resources.openRawResource(R.raw.test_recording_m4).readBytes()
        val fileName = "test_recording_m4.m4v"
        val metadata = UploadAudioRequestMetadata()

        CacophonyServer.uploadRecording(
            token,
            deviceName,
            fileName,
            file,
            metadata,
            {
                Log.i(TAG, it.toString())
                Toast.makeText(this, "Successful upload!", Toast.LENGTH_LONG).show()
                GlobalScope.launch {
                    database().recordingDao()
                        .insert(Recording(fileName, "0.000,9.999", metadata.recordingDataTime))
                    Log.i(TAG, database().recordingDao().getAll().joinToString())
                }
            },
            {
                Log.w(TAG, "Had error when uploading: $it")
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            })
    }
}
