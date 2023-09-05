package com.example.integratingsocketsinandroidkotlin.view.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.integratingsocketsinandroidkotlin.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

class LoginActivity : AppCompatActivity() {

    lateinit private var btn_login: Button
    lateinit private var et_email: TextView
    lateinit private var et_password: TextView
    lateinit var email: String
    lateinit var password: String

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_login = findViewById(R.id.btn_login)
        et_email = findViewById(R.id.et_email)
        et_password = findViewById(R.id.et_password)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()



        btn_login.setOnClickListener {
            email = et_email.text.toString()
            password = et_password.text.toString()

            // Query the userList collection for a matching email and password
            val userListRef = firestore.collection("UserList")


            userListRef.whereEqualTo("email", email).whereEqualTo("password", password).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val matchingUsers = task.result?.documents
                        if (matchingUsers != null && matchingUsers.isNotEmpty()) {
                            // Login successful
                            val id: String = Settings.Secure.getString(
                                contentResolver, Settings.Secure.ANDROID_ID
                            )
                            val userId = matchingUsers[0].id
                            updateDeviceIdIfNull(userId, id)
                        } else {
                            Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(this, "Query failed: ${task.exception}", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }
    }

    private fun updateDeviceIdIfNull(userId: String, id: String) {
        val userDocRef = firestore.collection("UserList").document(userId)

        // Use a transaction to update "deviceId" if it's null
        firestore.runTransaction { transaction ->
            val userDocSnapshot = transaction.get(userDocRef)
            val currentDeviceId = userDocSnapshot.getString("deviceId")

            if (currentDeviceId == null || currentDeviceId == id) {
                // Update "deviceId" field
                transaction.update(userDocRef, "deviceId", id)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }else {
                Toast.makeText(this, "This User login in another device", Toast.LENGTH_SHORT).show()
            }
        }
    }
}