package com.example.garbogo

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import android.content.res.Resources

class PostAd : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_ad)

        val categories = arrayOf("Electronics", "Furniture", "Clothing", "Toys")
        val conditions = arrayOf("New", "Used - Like New", "Used - Good Condition", "Used - Fair Condition")

        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        val conditionAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, conditions)

        val categoryEditText = findViewById<AutoCompleteTextView>(R.id.categoryEditText)
        val conditionEditText = findViewById<AutoCompleteTextView>(R.id.conditionEditText)

        categoryEditText.setAdapter(categoryAdapter)
        conditionEditText.setAdapter(conditionAdapter)

        // Set click listeners to show dropdown when AutoCompleteTextViews are clicked
        categoryEditText.setOnClickListener {
            categoryEditText.showDropDown()
        }

        conditionEditText.setOnClickListener {
            conditionEditText.showDropDown()
        }

        // Set click listener for the uploadImageIcon ImageView
        val uploadImageIcon = findViewById<ImageView>(R.id.uploadImageIcon)
        uploadImageIcon.setOnClickListener {
            dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            addImageToLayout(imageBitmap)
        }
    }

    private fun addImageToLayout(bitmap: Bitmap) {
        val photosLinearLayout = findViewById<LinearLayout>(R.id.photosLinearLayout)
        val imageView = ImageView(this)
        imageView.setImageBitmap(bitmap)
        val params = LinearLayout.LayoutParams(
            resources.dpToPx(100), // Adjust the width as needed
            resources.dpToPx(100) // Adjust the height as needed
        )
        params.setMargins(8, 0, 0, 0)
        imageView.layoutParams = params
        photosLinearLayout.addView(imageView)
    }


    fun Resources.dpToPx(dp: Int): Int {
        return (dp * this.displayMetrics.density).toInt()
    }

}
