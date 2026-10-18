package com.example.valmaro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class SignUpActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var sendOtpButton: Button
    private lateinit var signInText: TextView

    private lateinit var auth: FirebaseAuth

    private var verificationId: String? = null

    private var name: String = ""
    private var phone: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        nameInput = findViewById(R.id.nameInput)
        phoneInput = findViewById(R.id.phoneInput)
        sendOtpButton = findViewById(R.id.sendOtpButton)
        signInText = findViewById(R.id.signInText)


        // Send OTP
        sendOtpButton.setOnClickListener {

            name = nameInput.text.toString().trim()
            phone = phoneInput.text.toString().trim()

            if (name.isEmpty()) {

                nameInput.error = "Enter your name"
                return@setOnClickListener
            }

            if (phone.length != 10) {

                phoneInput.error =
                    "Enter valid 10 digit mobile number"

                return@setOnClickListener
            }

            // Check if number already registered
            checkExistingUser(phone)
        }


        // Sign In
        signInText.setOnClickListener {

            val intent = Intent(
                this,
                SignInActivity::class.java
            )

            startActivity(intent)
        }
    }


    // Check existing user
    private fun checkExistingUser(phoneNumber: String) {

        FirebaseDatabase.getInstance()
            .getReference("users")
            .orderByChild("phone")
            .equalTo(phoneNumber)
            .get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.exists()) {

                    // Number already registered
                    Toast.makeText(
                        this,
                        "This number is already registered. Please Sign In.",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addOnSuccessListener
                }

                // New number → Send OTP
                sendOTP(phoneNumber)
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    this,
                    "Database Error: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }


    // Send OTP
    private fun sendOTP(phoneNumber: String) {

        val fullPhoneNumber =
            "+91$phoneNumber"

        val options =
            PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(fullPhoneNumber)
                .setTimeout(
                    60L,
                    TimeUnit.SECONDS
                )
                .setActivity(this)
                .setCallbacks(
                    object :
                        PhoneAuthProvider
                        .OnVerificationStateChangedCallbacks() {

                        override fun onVerificationCompleted(
                            credential: PhoneAuthCredential
                        ) {

                            signInWithCredential(
                                credential
                            )
                        }


                        override fun onVerificationFailed(
                            e: FirebaseException
                        ) {

                            Toast.makeText(
                                this@SignUpActivity,
                                "OTP Error: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()

                            sendOtpButton.isEnabled = true
                        }


                        override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider
                            .ForceResendingToken
                        ) {

                            this@SignUpActivity
                                .verificationId =
                                verificationId

                            Toast.makeText(
                                this@SignUpActivity,
                                "OTP sent successfully",
                                Toast.LENGTH_SHORT
                            ).show()


                            val intent = Intent(
                                this@SignUpActivity,
                                OTPActivity::class.java
                            )

                            intent.putExtra(
                                "verificationId",
                                verificationId
                            )

                            intent.putExtra(
                                "name",
                                name
                            )

                            intent.putExtra(
                                "phone",
                                phone
                            )

                            intent.putExtra(
                                "mode",
                                "signup"
                            )

                            startActivity(intent)
                        }
                    }
                )
                .build()


        sendOtpButton.isEnabled = false

        PhoneAuthProvider
            .verifyPhoneNumber(options)
    }


    // Automatic verification
    private fun signInWithCredential(
        credential: PhoneAuthCredential
    ) {

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    Toast.makeText(
                        this,
                        "Phone verified successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    sendOtpButton.isEnabled = true

                    Toast.makeText(
                        this,
                        "Verification failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}