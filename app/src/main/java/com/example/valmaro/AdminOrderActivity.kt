package com.example.valmaro

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.valmaro.adapter.AdminOrderAdapter
import com.example.valmaro.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AdminOrderActivity : AppCompatActivity() {

    private lateinit var adminOrdersRecyclerView: RecyclerView

    private val allOrders = mutableListOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_admin_orders)

        adminOrdersRecyclerView =
            findViewById(R.id.adminOrdersRecyclerView)

        adminOrdersRecyclerView.layoutManager =
            LinearLayoutManager(this)

        loadAllOrders()
    }

    private fun loadAllOrders() {

        val currentUser =
            FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            Toast.makeText(
                this,
                "Please login first",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        FirebaseDatabase.getInstance()
            .getReference("users")
            .get()
            .addOnSuccessListener { usersSnapshot ->

                allOrders.clear()

                var completedUsers = 0
                val totalUsers = usersSnapshot.childrenCount.toInt()

                if (totalUsers == 0) {
                    showOrders()
                    return@addOnSuccessListener
                }

                for (userSnapshot in usersSnapshot.children) {

                    val uid = userSnapshot.key ?: continue

                    FirebaseDatabase.getInstance()
                        .getReference("orders")
                        .child(uid)
                        .get()
                        .addOnSuccessListener { ordersSnapshot ->

                            for (orderSnapshot in ordersSnapshot.children) {

                                val order =
                                    orderSnapshot.getValue(Order::class.java)

                                if (order != null) {
                                    allOrders.add(order)
                                }
                            }

                            completedUsers++

                            if (completedUsers >= totalUsers) {
                                showOrders()
                            }
                        }
                        .addOnFailureListener {

                            completedUsers++

                            if (completedUsers >= totalUsers) {
                                showOrders()
                            }
                        }
                }
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    this,
                    "Users load failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showOrders() {

        allOrders.sortByDescending {
            it.orderDate
        }

        adminOrdersRecyclerView.adapter =
            AdminOrderAdapter(allOrders)
    }
}