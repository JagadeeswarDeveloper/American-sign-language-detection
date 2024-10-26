package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class QuizActivity : AppCompatActivity() {

    private val randomLetter = ('A'..'Z').random().toString()
    private val requestCamera = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        Toast.makeText(this, "Please find the letter: $randomLetter", Toast.LENGTH_LONG).show()

        // Start camera
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, requestCamera)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCamera && resultCode == Activity.RESULT_OK) {
            // Here, you can use the recognition model to evaluate the camera input
            // If it matches the `randomLetter`, display success message
            val isCorrect = true // Placeholder for actual evaluation logic

            if (isCorrect) {
                Toast.makeText(this, "Correct! Well done!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Try again!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
