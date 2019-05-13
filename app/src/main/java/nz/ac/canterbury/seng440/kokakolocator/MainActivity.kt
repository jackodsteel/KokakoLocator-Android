package nz.ac.canterbury.seng440.kokakolocator

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import nz.ac.canterbury.seng440.kokakolocator.ui.login.LoginActivity
import nz.ac.canterbury.seng440.kokakolocator.ui.login.RegisterActivity
import nz.ac.canterbury.seng440.kokakolocator.util.TAG

const val PREFS_KEY = "PREFS"
const val TOKEN_KEY = "TOKEN"

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

        Log.i(TAG, token ?: "No token")


        fusedLocationClient = LocationServices.
            getFusedLocationProviderClient(this)
        getLocation()
    }

    fun getLocation(){
        if (checkPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            fusedLocationClient?.lastLocation?.
                addOnSuccessListener(this,
                    {location : Location? ->
                        // Got last known location. In some rare
                        // situations this can be null.
                        if(location == null) {
                            // TODO, handle it
                        } else location.apply {
                            // Handle location object
                            println(location.toString())
                            Log.e(TAG, location.toString())
                        }
                    })
        }

    }

    val PERMISSION_ID = 42
    private fun checkPermission(vararg perm:String) : Boolean {
        val havePermissions = perm.toList().all {
            ContextCompat.checkSelfPermission(this,it) ==
                    PackageManager.PERMISSION_GRANTED
        }
        if (!havePermissions) {
            if(perm.toList().any {
                    ActivityCompat.
                        shouldShowRequestPermissionRationale(this, it)}
            ) {
                val dialog = AlertDialog.Builder(this)
                    .setTitle("Permission")
                    .setMessage("Permission needed!")
                    .setPositiveButton("OK", {id, v ->
                        ActivityCompat.requestPermissions(
                            this, perm, PERMISSION_ID)
                    })
                    .setNegativeButton("No", {id, v -> })
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
