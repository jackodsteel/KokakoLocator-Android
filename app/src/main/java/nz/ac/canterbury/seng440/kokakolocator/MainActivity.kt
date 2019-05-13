package nz.ac.canterbury.seng440.kokakolocator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import nz.ac.canterbury.seng440.kokakolocator.ui.login.LoginActivity
import nz.ac.canterbury.seng440.kokakolocator.ui.login.RegisterActivity

const val PREFS_KEY = "PREFS"
const val TOKEN_KEY = "TOKEN"

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

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

        Log.i(TAG, token ?: "null")
    }
}
