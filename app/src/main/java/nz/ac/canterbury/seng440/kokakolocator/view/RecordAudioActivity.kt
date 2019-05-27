package nz.ac.canterbury.seng440.kokakolocator.view

import android.Manifest
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
import androidx.core.content.ContextCompat
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
import nz.ac.canterbury.seng440.kokakolocator.util.TAG
import nz.ac.canterbury.seng440.kokakolocator.util.prefs
import java.io.File
import java.io.IOException
import java.util.*


class RecordAudioActivity : AppCompatActivity() {
    private var output: String? = null
    private var isRecording: Boolean = false
    private var fileName:String = ""
    private var recordingLocation:Location? = null

    private var mediaRecorder: MediaRecorder? = null

    var fusedLocationClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_audio)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val imageButton = findViewById<ImageButton>(R.id.microphone)
        mediaRecorder = MediaRecorder()
        getLocation()

        imageButton.setOnClickListener {
            if (isRecording) {
                imageButton.setImageResource(R.drawable.microphone)
                stopRecording()
            }
            else {
                initializeRecorder()
                imageButton.setImageResource(R.drawable.microphone_activated)
                startRecording()
            }


        }

    }
    fun initializeRecorder(){
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        fileName = Calendar.getInstance().time.toString()
        output = filesDir.absolutePath + "/$fileName"
        mediaRecorder?.setOutputFile(output)




    }

    fun getCheckPermissions(){
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
            isRecording = false
            uploadAudioRecording(fileName)
        } else {
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }

    }

    private fun uploadAudioRecording(fileName:String) {

        val token = prefs().authToken
        val deviceName = prefs().deviceName

        if (token == null || deviceName == null) {
            Log.i(TAG, "token: $token, deviceName: $deviceName")
            return //TODO handle dis
        }


        val LatLng = LatLng(recordingLocation!!.latitude,recordingLocation!!.longitude)

        val metadata = UploadAudioRequestMetadata(location=LatLng)


        val file = File(output)

        CacophonyServer.uploadRecording(
            token,
            deviceName,
            file,
            metadata,
            {
                Log.i(TAG, it.toString())
                Log.i(TAG, it.recordingId)
                Log.i(TAG, "${it.recordingId.toLong()}")
                Toast.makeText(this, "Successful upload!", Toast.LENGTH_LONG).show()
                GlobalScope.launch {
                    database().recordingDao()
                        .insert(
                            Recording(
                                fileName,
                                LatLng,
                                Calendar.getInstance().time,
                                serverId = it.recordingId.toLong()
                            )
                        )
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
                    recordingLocation = location
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