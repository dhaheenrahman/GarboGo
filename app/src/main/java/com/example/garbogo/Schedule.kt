
package com.example.garbogo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.database.ktx.database

class Schedule : AppCompatActivity() {
    var i = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        val database = Firebase.database
        val nameField = findViewById<EditText>(R.id.nameField)
        val wardField = findViewById<EditText>(R.id.wardField)
        val houseField = findViewById<EditText>(R.id.houseField)
        val phoneField = findViewById<EditText>(R.id.phoneField)
        val datePrefField = findViewById<EditText>(R.id.dateField)
        val typeField = findViewById<EditText>(R.id.typeField)
        val scheduleBtn = findViewById<Button>(R.id.button)

        val type = intent.getStringExtra("Type") ?: ""
        typeField.setText(type)

        scheduleBtn.setOnClickListener(){
            val name = nameField.text.toString()
            val ward = wardField.text.toString()
            val house = houseField.text.toString()
            val phone = phoneField.text.toString()
            val datePref = datePrefField.text.toString()

            // Show a toast message
            Toast.makeText(this, "Pickup scheduled successfully", Toast.LENGTH_SHORT).show()

            // Write data to the database
            writeDataToDatabase(database, name, ward, house, phone, datePref, type)

            // Assuming you have an Intent to switch to HomeActivity, replace it accordingly
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

    }

    private fun writeDataToDatabase(database: FirebaseDatabase, name: String, ward: String, house: String, phone: String, datePref: String, type: String) {
        // Generate a serial number (you can implement your own logic to generate a unique serial number)
        val serialNumber = generateSerialNumber()

        // Set the serial number as the reference
        val databaseReference= database.getReference("pickup Request").child("Ward:$ward").child("Request:$i")

        databaseReference.child("Name").setValue(name)
        databaseReference.child("Ward_No").setValue(ward)
        databaseReference.child("House_No").setValue(house)
        databaseReference.child("Phone").setValue(phone)
        databaseReference.child("Preferred Date").setValue(datePref)
        databaseReference.child("Type").setValue(type)
    }

    private fun generateSerialNumber(): String {
        // Implement your own logic to generate a unique serial number
        // For example, you can use timestamp or a combination of timestamp and user ID
        // Here, I'm using the current timestamp as the serial number
        return System.currentTimeMillis().toString()
    }
}
