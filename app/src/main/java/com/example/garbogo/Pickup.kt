package com.example.garbogo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class Pickup : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickup)

        val switchPlastic = findViewById<Switch>(R.id.switch4)
        val switchPaper = findViewById<Switch>(R.id.switch5)
        val switchCloth = findViewById<Switch>(R.id.switch6)
        val switchElectronics = findViewById<Switch>(R.id.switch7)
        val switchMetals = findViewById<Switch>(R.id.switch8)
        val switchOthers = findViewById<Switch>(R.id.switch9)
        val nextButton = findViewById<Button>(R.id.loginUsr)

        nextButton.setOnClickListener {
            var type = ""
            when {
                switchPlastic.isChecked -> type = "Plastic"
                switchPaper.isChecked -> type = "Paper"
                switchCloth.isChecked -> type = "Cloth"
                switchElectronics.isChecked -> type = "Electronics"
                switchMetals.isChecked -> type = "Metals"
                switchOthers.isChecked -> type = "Others"
            }

            val intent = Intent(this, Schedule::class.java)
            intent.putExtra("Type", type)
            startActivity(intent)
        }
    }
}
