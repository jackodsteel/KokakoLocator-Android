package nz.ac.canterbury.seng440.kokakolocator.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng440.kokakolocator.R
import nz.ac.canterbury.seng440.kokakolocator.database.Recording
import nz.ac.canterbury.seng440.kokakolocator.database.database
import nz.ac.canterbury.seng440.kokakolocator.server.UploadAudioRequestMetadata
import nz.ac.canterbury.seng440.kokakolocator.server.cacophonyServer
import nz.ac.canterbury.seng440.kokakolocator.util.goTo
import nz.ac.canterbury.seng440.kokakolocator.util.prefs
import java.io.File
import java.util.*


class RecordAudioActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "RecordAudioActivity"

        private const val REQUIRED_PERMISSIONS_REQUEST_CODE = 9872
        private const val LOCATION_PERMISSIONS_REQUEST_CODE = 2987

        private const val AUDIO_FILE_EXTENSION = "mp4"

        private val REQUIRED_PERMISSIONS = listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        private val LOCATION_PERMISSIONS = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private lateinit var absoluteOutputFileName: String
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var imageButton: ImageButton

    private var isRecording: Boolean = false
    private var recordingLocation: Location? = null

    private var mediaRecorder: MediaRecorder = MediaRecorder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_audio)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        absoluteOutputFileName = "${filesDir.absolutePath}/${Calendar.getInstance().time}.$AUDIO_FILE_EXTENSION"
        Log.i(TAG, absoluteOutputFileName)
        imageButton = findViewById(R.id.microphone)

        getLocation()

        imageButton.setOnClickListener {
            if (isRecording) stopRecording() else startRecording()
        }
    }

    override fun onPause() {
        super.onPause()
        if (isRecording) stopRecording()
    }

    private fun initializeRecorder() {
        absoluteOutputFileName = "${filesDir.absolutePath}/${Calendar.getInstance().time}.$AUDIO_FILE_EXTENSION"
        mediaRecorder.setOutputFile(absoluteOutputFileName)

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

        recordingLocation?.let { mediaRecorder.setLocation(it.latitude.toFloat(), it.longitude.toFloat()) }

        val maxDurationSeconds = prefs().maxRecordLengthSeconds
        if (maxDurationSeconds != -1) {
            mediaRecorder.setMaxDuration(maxDurationSeconds * 1000)
            mediaRecorder.setOnInfoListener { _, what, _ ->
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    stopRecording()
                }
            }
        }
    }

    private fun startRecording() {
        if (!checkRequiredPermissions()) return
        try {
            initializeRecorder()
            mediaRecorder.prepare()
            mediaRecorder.start()
            isRecording = true
            imageButton.setImageResource(R.drawable.microphone_activated)
            Toast.makeText(this, getString(R.string.recording_started), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error when starting recording", e)
            Toast.makeText(this, getString(R.string.generic_error) + e, Toast.LENGTH_LONG).show()
        }
    }

    private fun stopRecording() {
        mediaRecorder.stop()
        imageButton.setImageResource(R.drawable.microphone)
        isRecording = false
        GlobalScope.launch {
            val recording = addRecordingToLocalDb()
            uploadAudioRecording(recording)
        }
    }

    private fun uploadAudioRecording(recording: Recording) {
        if (!prefs().autoUploadRecordings) return
        Log.d(TAG, "Started uploading recording to server: ${recording.id}")

        val token = prefs().authToken
        val deviceName = prefs().deviceName

        if (token == null || deviceName == null) {
            Log.e(TAG, "Wasn't logged in properly. Logging user out. token: $token, deviceName: $deviceName")
            Toast.makeText(this, getString(R.string.error_login_details), Toast.LENGTH_LONG).show()
            goTo(LandingActivity::class)
            return
        }

        val latLng = recordingLocation.let { if (it != null) LatLng(it.latitude, it.longitude) else null }

        val metadata = UploadAudioRequestMetadata(
            location = latLng,
            recordingDateTime = Calendar.getInstance().time
        )

        val file = File(absoluteOutputFileName)

        cacophonyServer().uploadRecording(
            token,
            deviceName,
            file,
            metadata,
            {
                Log.i(TAG, "Successfully uploaded recording: ${file.name}, server id: ${it.recordingId}")
                Toast.makeText(this, getString(R.string.upload_success), Toast.LENGTH_LONG).show()
                setServerRecordingId(recording, it.recordingId)
            },
            {
                Log.w(TAG, "Had error when uploading recording ID: ${recording.id}, $it")
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun addRecordingToLocalDb(): Recording {
        val latLng = recordingLocation.let { if (it != null) LatLng(it.latitude, it.longitude) else null }
        val recording = Recording(
            absoluteOutputFileName,
            latLng,
            Calendar.getInstance().time
        )
        Log.d(TAG, "Started adding recording to local DB: ${recording.fileName}")
        val id = database().recordingDao().insert(recording)
        Log.d(TAG, "Finished adding recording to local DB: ${recording.fileName}, id: $id")
        recording.id = id
        return recording
    }

    private fun setServerRecordingId(recording: Recording, serverId: Long) {
        GlobalScope.launch {
            recording.serverId = serverId
            database().recordingDao().update(recording)
            Log.d(TAG, "Added serverId to recording: ${recording.id}")
            Log.d(TAG, "Current state of DB: ${database().recordingDao().getAll()}")
        }
    }

    @SuppressLint("MissingPermission") // We do check them just not the way Android lint checks for
    private fun getLocation() {
        if (checkLocationPermissions()) {
            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location: Location? ->
                if (location != null) recordingLocation = location
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val denied = grantResults.zip(permissions).filter { it.first == PackageManager.PERMISSION_DENIED }
        when (requestCode) {
            REQUIRED_PERMISSIONS_REQUEST_CODE ->
                when {
                    denied.isEmpty() -> startRecording()
                    else -> AlertDialog.Builder(this)
                        .setTitle(getString(R.string.permissions_required_title))
                        .setMessage(getString(R.string.permissions_required_body))
                        .setPositiveButton(getString(R.string.permissions_required_yes)) { _, _ -> checkRequiredPermissions() }
                        .setNegativeButton(getString(R.string.no)) { _, _ -> }
                        .create()
                        .show()
                }
            LOCATION_PERMISSIONS_REQUEST_CODE -> {
                when {
                    denied.isEmpty() -> getLocation()
                    denied.any { ActivityCompat.shouldShowRequestPermissionRationale(this, it.second) } ->
                        AlertDialog.Builder(this)
                            .setTitle(getString(R.string.permissions_location_title))
                            .setMessage(getString(R.string.permissions_location_body))
                            .setPositiveButton(getString(R.string.permissions_required_yes)) { _, _ -> checkLocationPermissions() }
                            .setNegativeButton(getString(R.string.no)) { _, _ -> }
                            .create()
                            .show()
                }
            }
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return checkPermissions(LOCATION_PERMISSIONS, LOCATION_PERMISSIONS_REQUEST_CODE)
    }

    private fun checkRequiredPermissions(): Boolean {
        return checkPermissions(REQUIRED_PERMISSIONS, REQUIRED_PERMISSIONS_REQUEST_CODE)
    }

    private fun checkPermissions(permissions: List<String>, requestCode: Int): Boolean {
        val requiredPermissions = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (requiredPermissions.isEmpty()) {
            return true
        }
        Log.i(TAG, "Requesting permissions: $requiredPermissions")
        ActivityCompat.requestPermissions(this, requiredPermissions.toTypedArray(), requestCode)
        return false
    }

}