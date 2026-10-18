package com.example.valmaro

import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseTest {

    private val database: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("products")

    fun testConnection() {

        database.get().addOnSuccessListener { snapshot ->

            if (snapshot.exists()) {

                Log.d(
                    "FirebaseTest",
                    "Firebase connected successfully"
                )

                Log.d(
                    "FirebaseTest",
                    "Products found: ${snapshot.childrenCount}"
                )

            } else {

                Log.d(
                    "FirebaseTest",
                    "No products found"
                )
            }

        }.addOnFailureListener { error ->

            Log.e(
                "FirebaseTest",
                "Firebase Error: ${error.message}"
            )
        }
    }
}