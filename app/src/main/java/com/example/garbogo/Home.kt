package com.example.garbogo


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val button2 = findViewById<Button>(R.id.button90)
        val button3 = findViewById<Button>(R.id.button490)
        val button4 = findViewById<Button>(R.id.button990)
        val button5 = findViewById<Button>(R.id.button970)
        val combtn  = findViewById<Button>(R.id.communitybtn)
        // Set OnClickListener for Button 2

        combtn.setOnClickListener{
            val intent = Intent(this, Ecom::class.java)
            startActivity(intent)
        }

        button2.setOnClickListener {
            // Start MapActivity when Button 2 is clicked
            val intent = Intent(this, Map::class.java)
            startActivity(intent)
        }

        // Set OnClickListener for Button 3
        button3.setOnClickListener {
            // Sta0rt MapActivity when Button 3 is clicked
            val intent = Intent(this, Recycle::class.java)
            startActivity(intent)
        }
        button4.setOnClickListener{
            val intent = Intent(this,Illegal::class.java)
            startActivity(intent)
        }

        button5.setOnClickListener {
            val intent = Intent(this, Pickup::class.java)
            startActivity(intent)
        }
    }
}