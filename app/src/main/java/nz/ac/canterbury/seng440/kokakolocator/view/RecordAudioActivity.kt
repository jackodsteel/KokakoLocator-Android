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
import nz.ac.canterbury.seng440.kokakolocator.server.CacophonyServer
import nz.ac.canterbury.seng440.kokakolocator.server.UploadAudioRequestMetadata
import nz.ac.canterbury.seng440.kokakolocator.server.UploadAudioResponseBody
import nz.ac.canterbury.seng440.kokakolocator.util.TAG
import nz.ac.canterbury.seng440.kokakolocator.util.goTo
import nz.ac.canterbury.seng440.kokakolocator.util.prefs
import java.io.File
import java.util.*


class RecordAudioActivity : AppCompatActivity() {

    companion object {
        private const val REQUIRED_PERMISSIONS_REQUEST_CODE = 9872
        private const val LOCATION_PERMISSIONS_REQUEST_CODE = 2987

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
        absoluteOutputFileName = "${filesDir.absolutePath}/${Calendar.getInstance().time}"
        imageButton = findViewById(R.id.microphone)

        getLocation()

        imageButton.setOnClickListener {
            if (isRecording) stopRecording() else startRecording()
        }
    }

    private fun initializeRecorder() {
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        absoluteOutputFileName = "${filesDir.absolutePath}/${Calendar.getInstance().time}"
        mediaRecorder.setOutputFile(absoluteOutputFileName)
    }

    private fun startRecording() {
        if (!checkRequiredPermissions()) return
        try {
            initializeRecorder()
            mediaRecorder.prepare()
            mediaRecorder.start()
            isRecording = true
            imageButton.setImageResource(R.drawable.microphone_activated)
            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show() //TODO string var
        } catch (e: Exception) {
            Log.e(TAG, "Error when starting recording", e)
            Toast.makeText(this, "Something went wrong! $e", Toast.LENGTH_LONG).show() //TODO string var
        }
    }

    private fun stopRecording() {
        imageButton.setImageResource(R.drawable.microphone)
        if (isRecording) {
            mediaRecorder.stop()
            isRecording = false
            uploadAudioRecording()
        } else {
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show() //TODO string var
        }
    }

    private fun uploadAudioRecording() {
        if (!prefs().autoUploadRecordings) return

        val token = prefs().authToken
        val deviceName = prefs().deviceName

        if (token == null || deviceName == null) {
            Log.e(TAG, "Wasn't logged in properly. Logging user out. token: $token, deviceName: $deviceName")
            Toast.makeText(
                this,
                "There was an issue with your login info. Please log in again.", //TODO string var
                Toast.LENGTH_LONG
            ) //TODO string var
                .show()
            goTo(LandingActivity::class)
            return
        }

        val latLng = recordingLocation.let { if (it != null) LatLng(it.latitude, it.longitude) else null }

        val metadata = UploadAudioRequestMetadata(
            location = latLng,
            recordingDateTime = Calendar.getInstance().time
        )

        val file = File(absoluteOutputFileName)

        CacophonyServer.uploadRecording(
            token,
            deviceName,
            file,
            metadata,
            {
                Log.i(TAG, it.toString())
                Log.i(TAG, it.recordingId)
                Log.i(TAG, "${it.recordingId.toLong()}")
                Toast.makeText(this, "Successful upload!", Toast.LENGTH_LONG).show() //TODO string var
                addRecordingToLocalDb(it, latLng)
            },
            {
                Log.w(TAG, "Had error when uploading: $it")
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            })
    }

    private fun addRecordingToLocalDb(uploadAudioResponseBody: UploadAudioResponseBody, latLng: LatLng?) {
        GlobalScope.launch {
            database().recordingDao().insert(
                Recording(
                    absoluteOutputFileName,
                    latLng,
                    Calendar.getInstance().time,
                    serverId = uploadAudioResponseBody.recordingId.toLong()
                )
            )
            Log.i(TAG, database().recordingDao().getAll().joinToString()) // TODO debug only, remove
        }
    }


    @SuppressLint("MissingPermission") // We do check them just not the way Android checks for
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
                        .setTitle("Cannot run without permissions") //TODO string var
                        .setMessage("These permissions are required for the app to function correctly.") //TODO string var
                        .setPositiveButton("Add permissions") { _, _ -> checkRequiredPermissions() } //TODO string var
                        .setNegativeButton("No") { _, _ -> } //TODO string var
                        .create()
                        .show()
                }
            LOCATION_PERMISSIONS_REQUEST_CODE -> {
                when {
                    denied.isEmpty() -> getLocation()
                    denied.any { ActivityCompat.shouldShowRequestPermissionRationale(this, it.second) } ->
                        AlertDialog.Builder(this)
                            .setTitle("Location permissions") //TODO string var
                            .setMessage("Your location will not be recorded when making a recording without these permissions.") //TODO string var
                            .setPositiveButton("Add permissions") { _, _ -> checkLocationPermissions() } //TODO string var
                            .setNegativeButton("No") { _, _ -> } //TODO string var
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