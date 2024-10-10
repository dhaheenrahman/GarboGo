package com.example.garbogo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream

class Illegal : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sizeSwitches: List<Switch>
    private lateinit var typeSwitches: List<Switch>

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                // Permissions granted, proceed with the app flow
            } else {
                // Permissions denied, handle accordingly (e.g., display a message)
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_illegal)

        imageView = findViewById(R.id.imageButton)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Request necessary permissions
        if (!hasPermissions()) {
            requestPermissions()
        }

        // Set click listeners for image capture options
        imageView.setOnClickListener {
            captureImage()
        }

        sizeSwitches = listOf(findViewById(R.id.switch1), findViewById(R.id.switch2), findViewById(R.id.switch3))
        typeSwitches = listOf(findViewById(R.id.switch4), findViewById(R.id.switch5), findViewById(R.id.switch6))

        // Set click listener for submit button
        findViewById<Button>(R.id.submit).setOnClickListener {
            // Capture image and upload to Firebase
            val imageBitmap = (imageView.drawable as? BitmapDrawable)?.bitmap
            imageBitmap?.let { bitmap ->
                val size = when {
                    findViewById<Switch>(R.id.switch1).isChecked -> "Fits in a bag"
                    findViewById<Switch>(R.id.switch2).isChecked -> "Fits in a wheelbarrow"
                    findViewById<Switch>(R.id.switch3).isChecked -> "Fits in a truck"
                    else -> ""
                }
                val type = when {
                    findViewById<Switch>(R.id.switch4).isChecked -> "Solid Waste"
                    findViewById<Switch>(R.id.switch5).isChecked -> "Industrial Waste"
                    findViewById<Switch>(R.id.switch6).isChecked -> "Hazardous Waste"
                    else -> ""
                }
                uploadImageToFirebase(bitmap, size, type)
            } ?: run {
                Toast.makeText(this, "Please capture an image first", Toast.LENGTH_SHORT).show()
            }
        }
        // Set click listeners for size switches
        sizeSwitches.forEach { switch ->
            switch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    sizeSwitches.filter { it != switch }.forEach { it.isChecked = false }
                }
            }
        }

        // Set click listeners for type switches
        typeSwitches.forEach { switch ->
            switch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    typeSwitches.filter { it != switch }.forEach { it.isChecked = false }
                }
            }
        }
    }

    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )
    }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                imageView.setImageBitmap(imageBitmap)
            }
        }

    private fun captureImage() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(takePictureIntent)
    }

    private fun uploadImageToFirebase(bitmap: Bitmap, size: String, type: String) {
        val storageRef = Firebase.storage.reference.child("images/${System.currentTimeMillis()}.jpg")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        saveImageLocally(bitmap)

        // Add size and type fields to the data
        val data = hashMapOf(
            "size" to size,
            "type" to type
        )

        storageRef.putBytes(imageData)
            .addOnSuccessListener { taskSnapshot ->
                // Get the download URL from the task snapshot
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    // Add the download URL to the data
                    data["imageUrl"] = downloadUrl

                    // Upload the data to Firestore
                    firestore.collection("illegalDump").add(data)
                        .addOnSuccessListener { documentReference ->
                            Log.d("UPLOAD_IMAGE", "DocumentSnapshot added with ID: ${documentReference.id}")
                            // Pass the document ID to AuthIllegal activity
                            val intent = Intent(this, AuthIllegal::class.java)
                            intent.putExtra("issueDocumentId", documentReference.id)
                            startActivity(intent)
                            // Optionally, you can retrieve the document ID and use it to associate with other data
                            val documentId = documentReference.id
                            // Now, you can use the document ID to fetch this data along with the image in another activity
                            // Show success message and navigate to Home activity
                            Toast.makeText(this, "Issue reported successfully", Toast.LENGTH_SHORT).show()
                            navigateToHomeActivity()
                        }
                        .addOnFailureListener { e ->
                            Log.e("UPLOAD_IMAGE", "Error adding document", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("UPLOAD_IMAGE", "Error uploading image", e)
            }

    }

    private fun saveImageLocally(bitmap: Bitmap) {
        // Save the image to the device's external storage directory
        val fileName = "GarbageImage_${System.currentTimeMillis()}.jpg"
        val resolver = contentResolver

        // Save bitmap to MediaStore
        MediaStore.Images.Media.insertImage(resolver, bitmap, fileName, "Garbage Image")
    }

    private fun navigateToHomeActivity() {
        val intent = Intent(this, Home::class.java)
        startActivity(intent)
        finish()
    }
}
