package com.example.valmaro.manager

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object WatchlistManager {

    private val database =
        FirebaseDatabase.getInstance().reference

    private fun getWatchlistReference() =
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            database.child("users")
                .child(uid)
                .child("watchlist")
        }

    // =========================
    // ADD PRODUCT TO WATCHLIST
    // =========================

    fun addToWatchlist(
        productId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (productId.isBlank()) {
            onError("Product ID is missing")
            return
        }

        val reference = getWatchlistReference()

        if (reference == null) {
            onError("Please login first")
            return
        }

        reference.child(productId)
            .setValue(true)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to add product")
            }
    }

    // =========================
    // REMOVE PRODUCT
    // =========================

    fun removeFromWatchlist(
        productId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (productId.isBlank()) {
            onError("Product ID is missing")
            return
        }

        val reference = getWatchlistReference()

        if (reference == null) {
            onError("Please login first")
            return
        }

        reference.child(productId)
            .removeValue()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to remove product")
            }
    }

    // =========================
    // CHECK PRODUCT
    // =========================

    fun isInWatchlist(
        productId: String,
        onResult: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        if (productId.isBlank()) {
            onError("Product ID is missing")
            return
        }

        val reference = getWatchlistReference()

        if (reference == null) {
            onError("Please login first")
            return
        }

        reference.child(productId)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(
                    snapshot.getValue(Boolean::class.java) == true
                )
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to check watchlist")
            }
    }

    // =========================
    // GET WATCHLIST COUNT
    // =========================

    fun getWatchlistCount(
        onResult: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        val reference = getWatchlistReference()

        if (reference == null) {
            onError("Please login first")
            return
        }

        reference.get()
            .addOnSuccessListener { snapshot ->
                val count = snapshot.children.count {
                    it.getValue(Boolean::class.java) == true
                }

                onResult(count)
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to get watchlist count")
            }
    }
}