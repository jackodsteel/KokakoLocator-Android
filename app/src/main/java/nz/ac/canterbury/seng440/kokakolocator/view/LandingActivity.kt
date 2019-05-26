package nz.ac.canterbury.seng440.kokakolocator.view

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import nz.ac.canterbury.seng440.kokakolocator.R
import nz.ac.canterbury.seng440.kokakolocator.util.goTo

class LandingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.loginButton).setOnClickListener { goTo(LoginActivity::class) }
        findViewById<Button>(R.id.registerButton).setOnClickListener { goTo(RegisterActivity::class) }
    }

}
