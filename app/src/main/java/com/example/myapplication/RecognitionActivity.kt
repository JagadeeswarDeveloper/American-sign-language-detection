package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.ml.SsdMobilenetV11Metadata1 // Replace with your model name
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class RecognitionActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var bitmap: Bitmap
    private lateinit var labels: List<String>
    private lateinit var model: SsdMobilenetV11Metadata1 // Replace with your model class
    private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR)) // Adjust size to match your model's input
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recognition)

        // Load labels from the file (labels.txt from Teachable Machine export)
        labels = FileUtil.loadLabels(this, "labels.txt")
        model = SsdMobilenetV11Metadata1.newInstance(this)

        imageView = findViewById(R.id.imageView)

        // Buttons for selecting image input
        findViewById<Button>(R.id.btnCamera).setOnClickListener { openCamera() }
        findViewById<Button>(R.id.btnGallery).setOnClickListener { openGallery() }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val extras = data?.extras
                    if (extras != null) {
                        bitmap = extras.get("data") as Bitmap
                        getPredictions()
                    }
                }
                GALLERY_REQUEST_CODE -> {
                    val uri = data?.data
                    if (uri != null) {
                        bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                        getPredictions()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getPredictions() {
        // Create a TensorImage from the Bitmap
        var image = TensorImage.fromBitmap(bitmap)

        // Apply any preprocessing (resizing, normalization) via ImageProcessor
        image = imageProcessor.process(image)

        // Convert TensorImage to TensorBuffer (needed for model processing)
        val inputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), org.tensorflow.lite.DataType.UINT8) // Adjust size based on your model
        inputBuffer.loadBuffer(image.buffer) // Load the TensorImage buffer into the TensorBuffer

        // Process the inputBuffer using the model
        val outputs = model.process(inputBuffer) // Ensure you're using the correct model class and method

        // Assuming the model output is a TensorBuffer
        val probabilityBuffer = outputs.outputFeature0AsTensorBuffer // This is typically the output method for classification models

        // Extract probabilities from the TensorBuffer
        val probabilities = probabilityBuffer.floatArray

        // Find the index of the highest probability
        val maxIndex = probabilities.withIndex().maxByOrNull { it.value }?.index ?: -1
        val detectedLabel = if (maxIndex != -1) labels[maxIndex] else "Unknown"
        val confidence = probabilities[maxIndex]

        if (confidence > 0.5) { // Adjust threshold as needed
            Toast.makeText(this, "Detected Object: $detectedLabel with confidence $confidence", Toast.LENGTH_LONG).show()
            Log.d("DetectedLabel", "Detected Object: $detectedLabel with confidence $confidence")
        } else {
            Toast.makeText(this, "Low confidence detection", Toast.LENGTH_SHORT).show()
        }

        imageView.setImageBitmap(bitmap) // Show the original image
    }

    override fun onDestroy() {
        super.onDestroy()
        model.close() // Close the model when no longer needed
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 102
        private const val GALLERY_REQUEST_CODE = 101
    }
}
