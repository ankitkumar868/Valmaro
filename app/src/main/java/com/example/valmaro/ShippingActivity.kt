package com.example.valmaro

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ShippingActivity : AppCompatActivity() {

    private lateinit var nameInput: TextView
    private lateinit var phoneInput: TextView
    private lateinit var addressInput: EditText
    private lateinit var cityInput: EditText
    private lateinit var stateInput: EditText
    private lateinit var pinCodeInput: EditText
    private lateinit var landmarkInput: EditText
    private lateinit var saveAddressButton: Button

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shipping)

        nameInput = findViewById(R.id.nameInput)
        phoneInput = findViewById(R.id.phoneInput)
        addressInput = findViewById(R.id.addressInput)
        cityInput = findViewById(R.id.cityInput)
        stateInput = findViewById(R.id.stateInput)
        pinCodeInput = findViewById(R.id.pinCodeInput)
        landmarkInput = findViewById(R.id.landmarkInput)
        saveAddressButton = findViewById(R.id.saveAddressButton)

        loadUserDetails()
        loadShippingAddress()

        saveAddressButton.setOnClickListener {
            saveShippingAddress()
        }
    }

    private fun loadUserDetails() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .get()
            .addOnSuccessListener { snapshot ->

                val name = snapshot.child("name").getValue(String::class.java) ?: ""
                val phone = snapshot.child("phone").getValue(String::class.java) ?: ""

                nameInput.setText(name)
                phoneInput.setText(phone)
            }
    }
    private fun saveShippingAddress() {

        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(
                this,
                "Please login first",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val name = nameInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val address = addressInput.text.toString().trim()
        val city = cityInput.text.toString().trim()
        val state = stateInput.text.toString().trim()
        val pinCode = pinCodeInput.text.toString().trim()
        val landmark = landmarkInput.text.toString().trim()

        if (
            name.isEmpty() ||
            phone.length != 10 ||
            address.isEmpty() ||
            city.isEmpty() ||
            state.isEmpty() ||
            pinCode.length != 6
        ) {
            Toast.makeText(
                this,
                "Please enter valid address details",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val shippingData = mapOf(
            "name" to name,
            "phone" to phone,
            "address" to address,
            "city" to city,
            "state" to state,
            "pinCode" to pinCode,
            "landmark" to landmark
        )

        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(user.uid)
            .child("shippingAddress")
            .setValue(shippingData)
            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Shipping Address Saved Successfully",
                    Toast.LENGTH_SHORT
                ).show()
                finish()

            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    this,
                    "Error: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun loadShippingAddress() {

        val user = auth.currentUser ?: return

        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(user.uid)
            .child("shippingAddress")
            .get()
            .addOnSuccessListener { snapshot ->

                nameInput.setText(
                    snapshot.child("name").getValue(String::class.java) ?: ""
                )

                phoneInput.setText(
                    snapshot.child("phone").getValue(String::class.java) ?: ""
                )

                addressInput.setText(
                    snapshot.child("address").getValue(String::class.java) ?: ""
                )

                cityInput.setText(
                    snapshot.child("city").getValue(String::class.java) ?: ""
                )

                stateInput.setText(
                    snapshot.child("state").getValue(String::class.java) ?: ""
                )

                pinCodeInput.setText(
                    snapshot.child("pinCode").getValue(String::class.java) ?: ""
                )

                landmarkInput.setText(
                    snapshot.child("landmark").getValue(String::class.java) ?: ""
                )
            }
    }
}