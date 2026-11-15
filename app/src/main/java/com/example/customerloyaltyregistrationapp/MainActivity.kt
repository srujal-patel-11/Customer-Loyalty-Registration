package com.example.customerloyaltyregistrationapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val email = findViewById<EditText>(R.id.etEmail)
        val password = findViewById<EditText>(R.id.etPassword)
        val login = findViewById<Button>(R.id.btnLogin)
        val signup = findViewById<Button>(R.id.btnSignup)

        // LOGIN EXISTING CUSTOMER
        login.setOnClickListener {

            val userEmail = email.text.toString().trim()
            val userPassword = password.text.toString()

            if (userEmail.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(this,
                    "Please enter email and password",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            login.isEnabled = false

            auth.signInWithEmailAndPassword(
                userEmail, userPassword
            ).addOnSuccessListener {

                startActivity(
                    Intent(this, CustomersActivity::class.java)
                )
                finish()

            }.addOnFailureListener { error ->

                login.isEnabled = true

                Toast.makeText(this,
                    error.message ?: "Login failed",
                    Toast.LENGTH_LONG).show()
            }
        }

        // OPEN CUSTOMER REGISTRATION PAGE
        signup.setOnClickListener {

            startActivity(
                Intent(this, CustomerActivity::class.java)
            )
        }
    }
}
