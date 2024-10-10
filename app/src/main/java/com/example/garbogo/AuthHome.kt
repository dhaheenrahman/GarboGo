package com.example.garbogo

//package com.example.garbogo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton

class AuthHome : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_home)

        val wastePickupBtn = findViewById<Button>(R.id.button)
        val illegalDumpBtn = findViewById<Button>(R.id.button2)

        wastePickupBtn.setOnClickListener(){
            val intent = Intent(this,ActivityRequests::class.java)
            startActivity(intent)
        }

        illegalDumpBtn.setOnClickListener(){
            val intent = Intent(this,AuthIllegal::class.java)
            startActivity(intent)
        }
    }

}