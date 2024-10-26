package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Navigate to MainMenuActivity after a delay
        Handler().postDelayed({
            startActivity(Intent(this, MainMenuActivity::class.java))
            finish() // Close this activity so the user can't return to it
        }, 3000) // 3-second delay
    }
}
