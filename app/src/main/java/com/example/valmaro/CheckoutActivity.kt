package com.example.valmaro

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CheckoutActivity : AppCompatActivity() {

    private lateinit var customerName: EditText
    private lateinit var phoneInput: EditText
    private lateinit var addressInput: EditText

    private lateinit var itemCount: TextView
    private lateinit var subtotal: TextView
    private lateinit var deliveryCharge: TextView
    private lateinit var finalTotal: TextView

    private lateinit var placeOrderButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_checkout)

        customerName = findViewById(R.id.customerName)
        phoneInput = findViewById(R.id.phoneInput)
        addressInput = findViewById(R.id.addressInput)

        itemCount = findViewById(R.id.itemCount)
        subtotal = findViewById(R.id.subtotal)
        deliveryCharge = findViewById(R.id.deliveryCharge)
        finalTotal = findViewById(R.id.finalTotal)

        placeOrderButton =
            findViewById(R.id.placeOrderButton)

        showOrderSummary()
        loadCheckoutDetails()

        placeOrderButton.setOnClickListener {
            placeOrder()
        }
    }

    private fun showOrderSummary() {

        val cartItems = CartManager.cartItems

        var totalItems = 0

        for (item in cartItems) {
            totalItems += item.quantity
        }

        val subTotalAmount = CartManager.getTotal()

        val delivery =
            if (subTotalAmount > 0) 40 else 0

        val total =
            subTotalAmount + delivery

        itemCount.text = "Items: $totalItems"
        subtotal.text = "Subtotal: ₹$subTotalAmount"
        deliveryCharge.text = "Delivery: ₹$delivery"
        finalTotal.text = "Total: ₹$total"
    }

    private fun loadCheckoutDetails() {

        val uid =
            FirebaseAuth.getInstance().currentUser?.uid
                ?: return

        val userRef =
            FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)

        userRef.get()
            .addOnSuccessListener { snapshot ->

                val name =
                    snapshot.child("name")
                        .getValue(String::class.java)
                        ?: ""

                val phone =
                    snapshot.child("phone")
                        .getValue(String::class.java)
                        ?: ""

                customerName.setText(name)
                phoneInput.setText(phone)

                val addressSnapshot =
                    snapshot.child("shippingAddress")

                val address =
                    addressSnapshot.child("address")
                        .getValue(String::class.java)
                        ?: ""

                val city =
                    addressSnapshot.child("city")
                        .getValue(String::class.java)
                        ?: ""

                val state =
                    addressSnapshot.child("state")
                        .getValue(String::class.java)
                        ?: ""

                val pinCode =
                    addressSnapshot.child("pinCode")
                        .getValue(String::class.java)
                        ?: ""

                addressInput.setText(
                    "$address, $city, $state - $pinCode"
                )
            }
    }

    private fun placeOrder() {

        val name =
            customerName.text.toString().trim()

        val phone =
            phoneInput.text.toString().trim()

        val address =
            addressInput.text.toString().trim()

        if (name.isEmpty()) {
            customerName.error = "Enter your name"
            return
        }

        if (phone.length != 10) {
            phoneInput.error =
                "Enter valid 10 digit mobile number"
            return
        }

        if (address.isEmpty()) {
            addressInput.error =
                "Enter your address"
            return
        }

        if (CartManager.cartItems.isEmpty()) {

            Toast.makeText(
                this,
                "Your cart is empty",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        placeOrderButton.isEnabled = false

        checkLatestStock(
            name,
            phone,
            address
        )
    }

    private fun checkLatestStock(
        name: String,
        phone: String,
        address: String
    ) {

        FirebaseDatabase.getInstance()
            .getReference("products")
            .get()
            .addOnSuccessListener { snapshot ->

                for (cartItem in CartManager.cartItems) {

                    val productId =
                        cartItem.product.productId

                    val firebaseProduct =
                        if (productId.isNotBlank()) {

                            snapshot.children.firstOrNull {
                                it.key == productId
                            }

                        } else {

                            // Old cart items ke liye name fallback
                            snapshot.children.firstOrNull {

                                val firebaseName =
                                    it.child("name")
                                        .getValue(String::class.java)

                                firebaseName?.equals(
                                    cartItem.product.name,
                                    ignoreCase = true
                                ) == true
                            }
                        }

                    if (firebaseProduct == null) {

                        placeOrderButton.isEnabled = true

                        Toast.makeText(
                            this,
                            "${cartItem.product.name} is no longer available",
                            Toast.LENGTH_LONG
                        ).show()

                        return@addOnSuccessListener
                    }

                    val latestStock =
                        firebaseProduct
                            .child("stock")
                            .getValue(Int::class.java)
                            ?: 0

                    if (latestStock <= 0) {

                        placeOrderButton.isEnabled = true

                        Toast.makeText(
                            this,
                            "${cartItem.product.name} is OUT OF STOCK",
                            Toast.LENGTH_LONG
                        ).show()

                        return@addOnSuccessListener
                    }

                    if (cartItem.quantity > latestStock) {

                        placeOrderButton.isEnabled = true

                        Toast.makeText(
                            this,
                            "${cartItem.product.name} has only $latestStock item(s) available",
                            Toast.LENGTH_LONG
                        ).show()

                        return@addOnSuccessListener
                    }
                }

                saveOrder(
                    name,
                    phone,
                    address
                )
            }
            .addOnFailureListener { error ->

                placeOrderButton.isEnabled = true

                Toast.makeText(
                    this,
                    "Stock check failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun saveOrder(
        name: String,
        phone: String,
        address: String
    ) {

        // Current cart ki copy
        val orderedItems =
            CartManager.cartItems.toList()

        // Order save karo
        for (item in orderedItems) {

            val finalPrice =
                if (item.product.discountPrice.isNotBlank()) {

                    item.product.discountPrice

                } else {

                    item.product.price
                }

            OrderManager.addOrder(
                productName = item.product.name,
                price = finalPrice,
                quantity = item.quantity,
                address = address,
                customerName = name,
                phone = phone
            )

            OrderManager.decreaseStock(
                productName = item.product.name,
                quantity = item.quantity
            )
        }

        // IMPORTANT:
        // Sirf ordered products cart se remove honge
        for (item in orderedItems) {

            CartManager.cartItems.remove(item)
        }

        // Updated cart SharedPreferences me save
        CartManager.saveCartFromCheckout()

        Toast.makeText(
            this,
            "Order placed successfully",
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }
}