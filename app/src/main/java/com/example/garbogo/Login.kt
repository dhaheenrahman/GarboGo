package com.example.garbogo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginUsr: Button = findViewById(R.id.loginSib)
        val loginAuth: Button = findViewById(R.id.loginAuth)
//
        loginUsr.setOnClickListener {
            // Navigate to MainActivity as user
            val intent = Intent(this@Login, MainActivity::class.java)
            startActivity(intent)
        }

        loginAuth.setOnClickListener {
            // Navigate to MainActivity as authority
            val intent = Intent(this@Login, AuthHome::class.java)
            startActivity(intent)
        }
    }
}