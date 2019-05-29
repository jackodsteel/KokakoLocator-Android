package nz.ac.canterbury.seng440.kokakolocator.view

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import nz.ac.canterbury.seng440.kokakolocator.R
import nz.ac.canterbury.seng440.kokakolocator.server.LoginResponseBody
import nz.ac.canterbury.seng440.kokakolocator.server.cacophonyServer
import nz.ac.canterbury.seng440.kokakolocator.util.goTo
import nz.ac.canterbury.seng440.kokakolocator.util.prefs

class LoginActivity : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var login: Button
    private lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        login = findViewById(R.id.register)
        loading = findViewById(R.id.loading)


        password.apply {

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        login()
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                login()
            }
        }
    }

    private fun login() {
        cacophonyServer().login(
            username.text.toString(),
            password.text.toString(),
            this::onLoginSuccess,
            this::showLoginFailed
        )
    }

    private fun onLoginSuccess(response: LoginResponseBody) {
        val prefs = prefs()

        prefs.authToken = response.token
            val usernameStr = username.text.toString()
        prefs.username = usernameStr
        prefs.groupName = "${usernameStr}_default"
        prefs.deviceName = "${usernameStr}_default_device"
        val welcome = getString(R.string.welcome)
        Toast.makeText(
            applicationContext,
            welcome,
            Toast.LENGTH_LONG
        ).show()
        goTo(MainActivity::class)
    }

    private fun showLoginFailed(errorString: String) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
        loading.visibility = View.INVISIBLE
    }
}