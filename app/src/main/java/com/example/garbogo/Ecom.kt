package com.example.garbogo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout

class Ecom : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ecom)

        val slidingWindow = findViewById<LinearLayout>(R.id.slidingWindow)
        val imageButton3 = findViewById<ImageButton>(R.id.imageButton3)
        val addPostButton = findViewById<Button>(R.id.addPostButton)

        // Show/hide sliding window when imageButton3 is clicked
        imageButton3.setOnClickListener {
            if (slidingWindow.visibility == View.VISIBLE) {
                slidingWindow.visibility = View.INVISIBLE
            } else {
                slidingWindow.visibility = View.VISIBLE
            }
        }

        // Open new activity when addPostButton is clicked
        addPostButton.setOnClickListener {
            val intent = Intent(this, PostAd::class.java)
            startActivity(intent)
        }
    }
}
