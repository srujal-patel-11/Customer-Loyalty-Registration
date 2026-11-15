package com.example.customerloyaltyregistrationapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class CustomerActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val name = findViewById<EditText>(R.id.etName)
        val phone = findViewById<EditText>(R.id.etPhone)
        val dob = findViewById<EditText>(R.id.etDob)
        val email = findViewById<EditText>(R.id.etCustomerEmail)
        val address = findViewById<EditText>(R.id.etAddress)
        val password = findViewById<EditText>(R.id.etRegPassword)
        val confirmPassword =
            findViewById<EditText>(R.id.etConfirmPassword)

        val submit = findViewById<Button>(R.id.btnSubmit)

        // DATE OF BIRTH PICKER
        dob.setOnClickListener {

            val calendar = Calendar.getInstance()

            DatePickerDialog(
                this,
                { _, year, month, day ->

                    dob.setText(
                        String.format(
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            day
                        )
                    )

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)

            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }

        // SUBMIT CUSTOMER REGISTRATION
        submit.setOnClickListener {

            val customerName = name.text.toString().trim()
            val customerPhone = phone.text.toString().trim()
            val customerDob = dob.text.toString().trim()
            val customerEmail = email.text.toString().trim()
            val customerAddress = address.text.toString().trim()
            val customerPassword = password.text.toString()
            val confirm = confirmPassword.text.toString()

            // CHECK EMPTY FIELDS
            if (
                customerName.isEmpty() ||
                customerPhone.isEmpty() ||
                customerDob.isEmpty() ||
                customerEmail.isEmpty() ||
                customerAddress.isEmpty() ||
                customerPassword.isEmpty() ||
                confirm.isEmpty()
            ) {
                Toast.makeText(
                    this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // VALIDATE EMAIL
            if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(customerEmail).matches()) {

                email.error = "Enter a valid email address"
                return@setOnClickListener
            }

            // VALIDATE PHONE
            if (!customerPhone.matches(Regex("[0-9]{10}"))) {

                phone.error = "Enter a valid 10-digit phone number"
                return@setOnClickListener
            }

            // VALIDATE PASSWORD
            if (customerPassword.length < 6) {

                password.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            // CONFIRM PASSWORD
            if (customerPassword != confirm) {

                confirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            }

            // CREATE FIREBASE AUTH ACCOUNT
            submit.isEnabled = false
            submit.text = "PLEASE WAIT..."

            auth.createUserWithEmailAndPassword(
                customerEmail,
                customerPassword
            ).addOnSuccessListener { result ->

                val user = result.user

                if (user == null) {
                    submit.isEnabled = true
                    submit.text = "SUBMIT REGISTRATION"
                    Toast.makeText(
                        this,
                        "Account creation failed",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addOnSuccessListener
                }

                val customer = hashMapOf(
                    "uid" to user.uid,
                    "name" to customerName,
                    "phoneNo" to customerPhone,
                    "dob" to customerDob,
                    "email" to customerEmail,
                    "address" to customerAddress,
                    "createdAt" to FieldValue.serverTimestamp()
                )

                // SAVE CUSTOMER DATA IN FIRESTORE
                db.collection("customers")
                    .document(user.uid)
                    .set(customer)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Customer data saved successfully!",
                            Toast.LENGTH_LONG
                        ).show()

                        startActivity(
                            Intent(this, CustomersActivity::class.java)
                        )
                        finish()
                    }
                    .addOnFailureListener { error ->

                        android.util.Log.e(
                            "FIRESTORE_ERROR",
                            "Customer data save failed",
                            error
                        )

                        Toast.makeText(
                            this,
                            "Firestore Error: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnSuccessListener {

                        Toast.makeText(
                            this,
                            "Registration successful!",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(
                            this,
                            CustomersActivity::class.java
                        )

                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { error ->

                        submit.isEnabled = true
                        submit.text = "SUBMIT REGISTRATION"

                        Toast.makeText(
                            this,
                            "Account created, but customer data could not be saved: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

            }.addOnFailureListener { error ->

                submit.isEnabled = true
                submit.text = "SUBMIT REGISTRATION"

                Toast.makeText(
                    this,
                    error.message ?: "Registration failed",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
