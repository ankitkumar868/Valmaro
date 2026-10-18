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

class SignInActivity : AppCompatActivity() {

    private lateinit var phoneInput: EditText
    private lateinit var sendOtpButton: Button
    private lateinit var signUpText: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()

        phoneInput = findViewById(R.id.phoneInput)
        sendOtpButton = findViewById(R.id.sendOtpButton)
        signUpText = findViewById(R.id.signUpText)


        // SEND OTP
        sendOtpButton.setOnClickListener {

            val phone =
                phoneInput.text.toString().trim()

            if (phone.length != 10) {

                phoneInput.error =
                    "Enter valid 10 digit mobile number"

                return@setOnClickListener
            }

            checkRegisteredUser(phone)
        }


        // SIGN UP
        signUpText.setOnClickListener {

            val intent = Intent(
                this,
                SignUpActivity::class.java
            )

            startActivity(intent)
        }
    }


    // Check registered user
    private fun checkRegisteredUser(phone: String) {

        FirebaseDatabase.getInstance()
            .getReference("users")
            .orderByChild("phone")
            .equalTo(phone)
            .get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.exists()) {

                    // Number registered
                    sendLoginOTP(phone)

                } else {

                    // Number not registered
                    Toast.makeText(
                        this,
                        "Number not registered. Please Sign Up.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    this,
                    "Database Error: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }


    // Send Login OTP
    private fun sendLoginOTP(phone: String) {

        val fullPhoneNumber =
            "+91$phone"

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

                            auth.signInWithCredential(
                                credential
                            ).addOnCompleteListener { task ->

                                if (task.isSuccessful) {

                                    openHome()

                                } else {

                                    sendOtpButton.isEnabled = true

                                    Toast.makeText(
                                        this@SignInActivity,
                                        "Login failed",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }


                        override fun onVerificationFailed(
                            e: FirebaseException
                        ) {

                            sendOtpButton.isEnabled = true

                            Toast.makeText(
                                this@SignInActivity,
                                "OTP Error: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }


                        override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider
                            .ForceResendingToken
                        ) {

                            Toast.makeText(
                                this@SignInActivity,
                                "OTP sent successfully",
                                Toast.LENGTH_SHORT
                            ).show()


                            val intent = Intent(
                                this@SignInActivity,
                                OTPActivity::class.java
                            )

                            intent.putExtra(
                                "verificationId",
                                verificationId
                            )

                            intent.putExtra(
                                "phone",
                                phone
                            )

                            intent.putExtra(
                                "mode",
                                "signin"
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


    // Open Home
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