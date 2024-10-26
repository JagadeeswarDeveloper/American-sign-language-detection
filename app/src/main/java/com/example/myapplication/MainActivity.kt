package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.ml.SsdMobilenetV11Metadata1 // Replace with your model class name
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class MainActivity : AppCompatActivity() {

    private lateinit var btnLettersNumbers: Button
    private lateinit var btnWords: Button
    private lateinit var chatbox: TextView
    private lateinit var backgroundImage: ImageView
    private lateinit var bitmap: Bitmap
    private lateinit var labels: List<String>
    private lateinit var model: SsdMobilenetV11Metadata1
    private var isWordMode = false
    private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
        .build()
    private val selectedInputs = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        labels = FileUtil.loadLabels(this, "labels.txt")
        model = SsdMobilenetV11Metadata1.newInstance(this)

        btnLettersNumbers = findViewById(R.id.btnLettersNumbers)
        btnWords = findViewById(R.id.btnWords)
        chatbox = findViewById(R.id.chatbox)
        backgroundImage = findViewById(R.id.backgroundImage)

        btnLettersNumbers.setOnClickListener {
            isWordMode = false
            showInputOptions()
        }

        btnWords.setOnClickListener {
            isWordMode = true
            showInputOptions()
        }
    }

    private fun showInputOptions() {
        val options = arrayOf("Gallery", "Camera")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Input Source")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> selectImageFromGallery()
                1 -> openCamera()
            }
        }
        builder.show()
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startActivityForResult(intent, 101)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, 102)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            bitmap = when (requestCode) {
                101 -> MediaStore.Images.Media.getBitmap(this.contentResolver, data?.data)
                102 -> data?.extras?.get("data") as Bitmap
                else -> return
            }
            processImage()
        }
    }

    private fun processImage() {
        val image = imageProcessor.process(TensorImage.fromBitmap(bitmap))
        val inputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), org.tensorflow.lite.DataType.UINT8)
        inputBuffer.loadBuffer(image.buffer)

        val outputs = model.process(inputBuffer)
        val probabilityBuffer = outputs.outputFeature0AsTensorBuffer
        val probabilities = probabilityBuffer.floatArray

        val maxIndex = probabilities.withIndex().maxByOrNull { it.value }?.index ?: -1
        val detectedLabel = if (maxIndex != -1) labels[maxIndex] else "Unknown"
        val confidence = probabilities[maxIndex]

        if (confidence > 0.5) {
            if (isWordMode) {
                selectedInputs.add(detectedLabel)
                chatbox.text = selectedInputs.joinToString(" ")
            } else {
                chatbox.text = detectedLabel
            }
            if (!isWordMode || userFinishedInput()) {
                finishInputSequence()
            }
        } else {
            Toast.makeText(this, "Low confidence detection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun userFinishedInput(): Boolean {
        // Logic to determine if the user is finished with input (e.g., max word length reached)
        return selectedInputs.size >= 5 // Example threshold for word completion
    }

    private fun finishInputSequence() {
        model.close()
        selectedInputs.clear()

        // Change the background image after input completion
        backgroundImage.setImageResource(R.drawable.img_1) // Replace with your final background image
        Toast.makeText(this, "Input Complete!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        model.close()
    }
}
