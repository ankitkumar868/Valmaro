package com.example.valmaro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase

class OTPActivity : AppCompatActivity() {

    private lateinit var otpInput: EditText
    private lateinit var verifyOtpButton: Button
    private lateinit var otpMessage: TextView

    private lateinit var auth: FirebaseAuth

    private var verificationId: String = ""
    private var name: String = ""
    private var phone: String = ""
    private var mode: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_otpactivity)

        otpInput = findViewById(R.id.otpInput)
        verifyOtpButton = findViewById(R.id.verifyOtpButton)
        otpMessage = findViewById(R.id.otpMessage)

        auth = FirebaseAuth.getInstance()

        verificationId =
            intent.getStringExtra("verificationId") ?: ""

        name =
            intent.getStringExtra("name") ?: ""

        phone =
            intent.getStringExtra("phone") ?: ""

        mode =
            intent.getStringExtra("mode") ?: "signin"

        otpMessage.text = "OTP sent to +91 $phone"

        verifyOtpButton.setOnClickListener {

            val otp = otpInput.text.toString().trim()

            if (otp.length != 6) {
                otpInput.error = "Enter valid 6 digit OTP"
                return@setOnClickListener
            }

            if (verificationId.isEmpty()) {
                Toast.makeText(
                    this,
                    "Verification ID missing",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            verifyOTP(otp)
        }
    }

    private fun verifyOTP(otp: String) {

        verifyOtpButton.isEnabled = false

        val credential: PhoneAuthCredential =
            PhoneAuthProvider.getCredential(
                verificationId,
                otp
            )

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    if (mode == "signup") {

                        saveUserProfile()

                    } else {

                        Toast.makeText(
                            this,
                            "Login successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        openHome()
                    }

                } else {

                    verifyOtpButton.isEnabled = true

                    Toast.makeText(
                        this,
                        "Invalid OTP: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun saveUserProfile() {

        val user = auth.currentUser

        if (user == null) {
            verifyOtpButton.isEnabled = true

            Toast.makeText(
                this,
                "User not found",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val uid = user.uid

        val profile = mapOf(
            "name" to name,
            "phone" to phone,
            "email" to "",
            "uid" to uid
        )

        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .setValue(profile)
            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Account created successfully",
                    Toast.LENGTH_SHORT
                ).show()

                openHome()
            }
            .addOnFailureListener { error ->

                verifyOtpButton.isEnabled = true

                Toast.makeText(
                    this,
                    "Profile save error: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun openHome() {

        val intent = Intent(
            this,
            MainActivity::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

        finish()
    }
}