package nz.ac.canterbury.seng440.kokakolocator

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng440.kokakolocator.database.Recording
import nz.ac.canterbury.seng440.kokakolocator.database.database
import nz.ac.canterbury.seng440.kokakolocator.server.CacophonyServer
import nz.ac.canterbury.seng440.kokakolocator.server.UploadAudioRequestMetadata
import nz.ac.canterbury.seng440.kokakolocator.ui.login.LoginActivity
import nz.ac.canterbury.seng440.kokakolocator.ui.login.RegisterActivity
import nz.ac.canterbury.seng440.kokakolocator.util.TAG

const val PREFS_KEY = "PREFS"
const val TOKEN_KEY = "TOKEN"
const val USERNAME_KEY = "USERNAME"
const val GROUP_NAME_KEY = "GROUP_NAME"
const val DEVICE_NAME_KEY = "DEVICE_NAME"

class MainActivity : AppCompatActivity() {

    var fusedLocationClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            val toLoginIntent = Intent(this, LoginActivity::class.java)
            startActivity(toLoginIntent)
        }
        val registerButton = findViewById<Button>(R.id.registerButton)
        registerButton.setOnClickListener {
            val toRegisterIntent = Intent(this, RegisterActivity::class.java)
            startActivity(toRegisterIntent)
        }

        val token: String? = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE).getString(TOKEN_KEY, null)
        val deviceName: String? = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE).getString(DEVICE_NAME_KEY, null)


        val uploadButton = findViewById<Button>(R.id.uploadButton)
        uploadButton.setOnClickListener {
            if (token == null || deviceName == null) {
                Toast.makeText(this, "Must login", Toast.LENGTH_LONG).show()
                return@setOnClickListener
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



        Log.i(TAG, token ?: "No token")


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLocation()
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
