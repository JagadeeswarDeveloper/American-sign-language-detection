package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class MainMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        findViewById<Button>(R.id.btnStartRecognizing).setOnClickListener {
            startActivity(Intent(this, RecognitionActivity::class.java))
        }

        findViewById<Button>(R.id.btnLearn).setOnClickListener {
            startActivity(Intent(this, LearnActivity::class.java))
        }

        findViewById<Button>(R.id.btnQuiz).setOnClickListener {
            startActivity(Intent(this, QuizActivity::class.java))
        }
    }
}
