package nz.ac.canterbury.seng440.kokakolocator.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng440.kokakolocator.R
import nz.ac.canterbury.seng440.kokakolocator.database.Recording
import nz.ac.canterbury.seng440.kokakolocator.database.database
import nz.ac.canterbury.seng440.kokakolocator.server.CacophonyServer
import nz.ac.canterbury.seng440.kokakolocator.server.UploadAudioRequestMetadata
import nz.ac.canterbury.seng440.kokakolocator.util.TAG
import java.io.IOException


class RecordAudioActivity : AppCompatActivity() {
    private var output: String? = null
    private var isRecording: Boolean = false

    private var mediaRecorder: MediaRecorder? = null

    var fusedLocationClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_audio)

        output = filesDir.absolutePath + "/recording.mp3"



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLocation()



        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(output)

        Log.e("Output", output)

        var isActivated = false

        val linearLayout = findViewById<LinearLayout>(R.id.RecordingLayout)
        val imageButton = findViewById<ImageButton>(R.id.microphone)
        imageButton.setOnClickListener {
            isActivated = isActivated.not()
            if (isActivated) {
                imageButton.setImageResource(R.drawable.microphone)
                stopRecording()
            } else {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    val permissions = arrayOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    ActivityCompat.requestPermissions(this, permissions, 0)
                } else {
                    startRecording()
                }
//                imageButton.setImageResource(R.drawable.microphone_activated)

            }

        }
    }

    fun startRecording() {
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            isRecording = true
            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun stopRecording() {
        if (isRecording) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            isRecording = false
        } else {
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }

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
                        .insert(Recording(fileName, "0.000,9.999", "2018-05-24:23-17-00"))
                    Log.i(TAG, database().recordingDao().getAll().joinToString())
                }
            },
            {
                Log.w(TAG, "Had error when uploading: $it")
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            })
    }


    fun getLocation() {
        if (checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)) {
            fusedLocationClient?.lastLocation?.addOnSuccessListener(this) { location: Location? ->
                // Got last known location. In some rare
                // situations this can be null.
                if (location == null) {
                    // TODO, handle it
                } else location.apply {
                    // Handle location object
                    println(location.toString())
                    Log.e(TAG, location.toString())
                }
            }
        }

    }

    val PERMISSION_ID = 42
    private fun checkPermission(vararg perm: String): Boolean {
        val havePermissions = perm.toList().all {
            ContextCompat.checkSelfPermission(this, it) ==
                    PackageManager.PERMISSION_GRANTED
        }
        if (!havePermissions) {
            if (perm.toList().any { ActivityCompat.shouldShowRequestPermissionRationale(this, it) }) {
                val dialog = AlertDialog.Builder(this)
                    .setTitle("Permission")
                    .setMessage("Permission needed!")
                    .setPositiveButton("OK") { _, _ -> ActivityCompat.requestPermissions(this, perm, PERMISSION_ID) }
                    .setNegativeButton("No") { _, _ -> }
                    .create()
                dialog.show()
            } else {
                ActivityCompat.requestPermissions(this, perm, PERMISSION_ID)
            }
            return false
        }
        return true
    }


}