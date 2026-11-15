package com.example.customerloyaltyregistrationapp

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CustomersActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var customerContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customers)

        db = FirebaseFirestore.getInstance()

        customerContainer =
            findViewById(R.id.customerContainer)

        findViewById<Button>(R.id.btnRefresh)
            .setOnClickListener {
                loadCustomers()
            }

        findViewById<Button>(R.id.btnLogout)
            .setOnClickListener {

                FirebaseAuth.getInstance().signOut()

                startActivity(
                    Intent(this, MainActivity::class.java)
                )
                finish()
            }

        loadCustomers()
    }


    private fun loadCustomers() {

        customerContainer.removeAllViews()

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            addMessage("Please login first.")
            return
        }

        db.collection("customers")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    addMessage("No customer record found.")
                    return@addOnSuccessListener
                }

                val name = document.getString("name") ?: "N/A"
                val phone = document.getString("phoneNo") ?: "N/A"
                val dob = document.getString("dob") ?: "N/A"
                val email = document.getString("email") ?: "N/A"
                val address = document.getString("address") ?: "N/A"

                val details = """
                $name

                Phone: $phone
                DOB: $dob
                Email: $email
                Address: $address
            """.trimIndent()

                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(20, 20, 20, 20)
                    setBackgroundColor(android.graphics.Color.WHITE)
                    elevation = 5f
                }

                val info = TextView(this).apply {
                    text = details
                    textSize = 15f
                    setTextColor(
                        android.graphics.Color.rgb(23, 43, 77)
                    )
                }

                card.addView(info)

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 18)
                }

                customerContainer.addView(card, params)
            }
            .addOnFailureListener { error ->
                android.util.Log.e(
                    "CUSTOMER_LOAD_ERROR",
                    "Unable to load customer",
                    error
                )

                addMessage("Unable to load: ${error.message}")
            }
    }


    private fun addMessage(message: String) {
        val text = TextView(this).apply {
            this.text = message
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(20, 30, 20, 30)
        }

        customerContainer.addView(text)
    }
}
