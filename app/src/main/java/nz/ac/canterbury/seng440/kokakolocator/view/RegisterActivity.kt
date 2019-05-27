package nz.ac.canterbury.seng440.kokakolocator.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import nz.ac.canterbury.seng440.kokakolocator.R
import nz.ac.canterbury.seng440.kokakolocator.server.CacophonyServer
import nz.ac.canterbury.seng440.kokakolocator.server.SuccessfulRegistrationData
import nz.ac.canterbury.seng440.kokakolocator.util.prefs

class RegisterActivity : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var register: Button
    private lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        username = findViewById(R.id.username)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        register = findViewById(R.id.register)
        loading = findViewById(R.id.loading)


        password.apply {

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        register()
                }
                false
            }

            register.setOnClickListener {
                loading.visibility = View.VISIBLE
                register()
            }
        }
    }

    private fun register() {
        CacophonyServer.register(
            username.text.toString(),
            email.text.toString(),
            password.text.toString(),
            this::onRegisterSuccess,
            this::showLoginFailed
        )
    }

    private fun onRegisterSuccess(response: SuccessfulRegistrationData) {
        val prefs = prefs()
        prefs.authToken = response.token
        prefs.username = response.username
        prefs.groupName = response.groupName
        prefs.deviceName = response.deviceName

        val welcome = getString(R.string.welcome)
        Toast.makeText(
            applicationContext,
            welcome,
            Toast.LENGTH_LONG
        ).show()
        val toMainIntent = Intent(this, MainActivity::class.java)
        startActivity(toMainIntent)
    }

    private fun showLoginFailed(errorString: String) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_LONG).show()
        loading.visibility = View.INVISIBLE
    }
}