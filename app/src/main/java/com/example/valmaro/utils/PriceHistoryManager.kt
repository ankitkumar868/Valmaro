package com.example.valmaro.utils

import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PriceHistoryManager {

    fun savePrice(
        productId: String,
        price: Double,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {

        if (productId.isBlank() || price <= 0) {
            onError("Invalid product ID or price")
            return
        }

        val productRef = FirebaseDatabase.getInstance()
            .getReference("products")
            .child(productId)

        val historyRef = productRef.child("priceHistory")

        val date = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).format(Date())

        historyRef.get()
            .addOnSuccessListener { snapshot ->

                // Aaj ka record pehle se hai to duplicate mat banao
                var alreadySaved = false

                for (item in snapshot.children) {
                    val savedDate = item.child("date")
                        .getValue(String::class.java)

                    if (savedDate == date) {
                        alreadySaved = true
                        break
                    }
                }

                if (alreadySaved) {
                    onSuccess()
                    return@addOnSuccessListener
                }

                val record = mapOf(
                    "price" to price,
                    "date" to date
                )

                historyRef.push()
                    .setValue(record)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { error ->
                        onError(error.message ?: "Unable to save price")
                    }
            }
            .addOnFailureListener { error ->
                onError(error.message ?: "Unable to load price history")
            }
    }
}