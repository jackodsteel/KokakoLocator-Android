package nz.ac.canterbury.seng440.kokakolocator.view

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng440.kokakolocator.R
import nz.ac.canterbury.seng440.kokakolocator.database.Recording
import nz.ac.canterbury.seng440.kokakolocator.database.database
import nz.ac.canterbury.seng440.kokakolocator.server.CacophonyServer
import nz.ac.canterbury.seng440.kokakolocator.server.UploadAudioRequestMetadata
import nz.ac.canterbury.seng440.kokakolocator.util.TAG
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
        //findViewById<Button>(R.id.goToRecordButton).setOnClickListener { goTo(LandingActivity::class) } //TODO remove this when not debugging
        findViewById<Button>(R.id.goToRecordingsButton).setOnClickListener { goTo(ViewRecordingsActivity::class) }
        findViewById<Button>(R.id.goToPrefsButton).setOnClickListener { goTo(PreferencesActivity::class) }
        //findViewById<Button>(R.id.goToPrefsButton).setOnClickListener { uploadAudioRecording() } // TODO remove this when not debugging

    }

    private fun isNotLoggedIn(): Boolean {
        val prefs = prefs()
        return prefs.authToken == null || prefs.deviceName == null
    }


    private fun uploadAudioRecording() {

        val prefs = prefs()
        val token = prefs.authToken
        val deviceName = prefs.deviceName

        if (token == null || deviceName == null) {
            Log.i(TAG, "token: $token, deviceName: $deviceName")
            return //TODO handle dis
        }

        val file = resources.openRawResource(R.raw.test_recording_m4).readBytes()
        val fileName = "test_recording_m4.m4v"
        val metadata = UploadAudioRequestMetadata(duration = 100L, location = LatLng(-37.805294, 175.306775))

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
                    val id = database().recordingDao()
                        .insert(
                            Recording(
                                fileName,
                                LatLng(0.0, 0.0),
                                metadata.recordingDateTime,
                                serverId = it.recordingId.toLong()
                            )
                        )
//                    Log.i(TAG, database().recordingDao().getAll().joinToString())
//                    database().recordingDao().getById(id)?.file?.let {f ->
//                        Log.i(TAG, "${f.name}, ${f.absoluteFile}")
//                    }
                }
            },
            {
                Log.w(TAG, "Had error when uploading: $it")
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            })
    }
}
