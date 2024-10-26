package com.example.myapplication

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LearnActivity : AppCompatActivity() {

    private lateinit var inputLetter: EditText
    private lateinit var displayImage: ImageView
    private lateinit var showButton: Button
    private val imageMap = mapOf(
        "A" to R.drawable.a, // Replace with your images
        "B" to R.drawable.b,
        // Add more mappings for each letter/number
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learn)

        inputLetter = findViewById(R.id.inputLetter)
        displayImage = findViewById(R.id.displayImage)
        showButton = findViewById(R.id.showButton)

        showButton.setOnClickListener {
            val letter = inputLetter.text.toString().uppercase()
            val imageRes = imageMap[letter]
            if (imageRes != null) {
                displayImage.setImageResource(imageRes)
            } else {
                Toast.makeText(this, "No image found for $letter", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
