package com.example.garbogo

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso

class AuthIllegal : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_illegal)

        firestore = FirebaseFirestore.getInstance()

        // Fetch and display all entries from Firestore
        fetchAllEntries()
    }

    private fun fetchAllEntries() {
        val layout: LinearLayout = findViewById(R.id.entriesLayout)

        firestore.collection("illegalDump")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val imageUrl = document.getString("imageUrl")
                    val size = document.getString("size")
                    val type = document.getString("type")
                    val documentId = document.id // Get the document ID

                    // Create views to display the data
                    val imageView = ImageView(this)
                    val sizeTextView = TextView(this)
                    val typeTextView = TextView(this)
                    val resolveButton = Button(this)

                    // Load image using Picasso
                    Picasso.get().load(imageUrl).into(imageView)

                    // Set text for size and type
                    sizeTextView.text = "Size: $size"
                    typeTextView.text = "Type: $type"

                    // Set text for resolve button
                    resolveButton.text = "Mark as resolved "

                    // Add views to the layout
                    layout.addView(imageView)
                    layout.addView(sizeTextView)
                    layout.addView(typeTextView)
                    layout.addView(resolveButton)

                    // Add click listener for resolve button
                    resolveButton.setOnClickListener {
                        // Remove the entry from Firestore
                        removeEntryFromFirestore(documentId)

                        // Delete the image from Firebase Storage
                        deleteImageFromStorage(imageUrl)

                        // Remove views from the layout
                        layout.removeView(imageView)
                        layout.removeView(sizeTextView)
                        layout.removeView(typeTextView)
                        layout.removeView(resolveButton)
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                // For example, display a toast message
                // Toast.makeText(this, "Error fetching documents: $exception", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeEntryFromFirestore(documentId: String) {
        firestore.collection("illegalDump").document(documentId)
            .delete()
            .addOnSuccessListener {
                // Entry removed successfully
            }
            .addOnFailureListener { exception ->
                // Handle failure
            }
    }

    private fun deleteImageFromStorage(imageUrl: String?) {
        if (!imageUrl.isNullOrEmpty()) {
            val storageRef = Firebase.storage.reference
            val imageRef = storageRef.child(imageUrl)
            imageRef.delete()
                .addOnSuccessListener {
                    // Image deleted successfully
                    Log.d("DELETE_IMAGE", "Image deleted successfully: $imageUrl")
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                    Log.e("DELETE_IMAGE", "Error deleting image: $imageUrl", exception)
                }
        } else {
            Log.w("DELETE_IMAGE", "Image URL is null or empty")
        }
    }

}
