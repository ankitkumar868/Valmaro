package com.example.valmaro

import com.example.valmaro.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object OrderManager {

    val orders = mutableListOf<Order>()

    private val database = FirebaseDatabase.getInstance()

    // =========================
    // ADD ORDER
    // =========================

    fun addOrder(
        productName: String,
        price: String,
        quantity: Int,
        address: String,
        customerName: String,
        phone: String,
    ) {

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            return
        }

        val uid = user.uid

        val orderId = "VM" + System.currentTimeMillis()

        // Current order time
        val currentTime = System.currentTimeMillis()

        // Estimated delivery after 5 days
        val estimatedDelivery =
            currentTime + (5 * 24 * 60 * 60 * 1000L)

        val order = Order(

            orderId = orderId,
            productName = productName,
            price = price,
            quantity = quantity,

            status = "Confirmed",

            address = address,
            customerName = customerName,
            phone = phone,

            orderDate = currentTime,

            // New delivery date field
            estimatedDeliveryDate = estimatedDelivery
        )

        // Local list
        orders.add(order)

        // Firebase
        database
            .getReference("orders")
            .child(uid)
            .child(orderId)
            .setValue(order)
    }

    // =========================
    // DECREASE STOCK
    // =========================

    fun decreaseStock(
        productName: String,
        quantity: Int
    ) {

        val productRef = database.getReference("products")

        productRef.get()
            .addOnSuccessListener { snapshot ->

                for (productSnapshot in snapshot.children) {

                    val name = productSnapshot
                        .child("name")
                        .getValue(String::class.java)

                    if (
                        name?.equals(
                            productName,
                            ignoreCase = true
                        ) == true
                    ) {

                        val currentStock = productSnapshot
                            .child("stock")
                            .getValue(Int::class.java)
                            ?: 0

                        val newStock =
                            (currentStock - quantity).coerceAtLeast(0)

                        productSnapshot.ref
                            .child("stock")
                            .setValue(newStock)

                        break
                    }
                }
            }
    }

    // =========================
    // UPDATE ORDER STATUS
    // =========================

    fun updateOrderStatus(
        orderId: String,
        newStatus: String
    ) {

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            return
        }

        val uid = user.uid

        database
            .getReference("orders")
            .child(uid)
            .child(orderId)
            .child("status")
            .setValue(newStatus)
    }
}