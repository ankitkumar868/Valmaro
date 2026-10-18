package com.example.valmaro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.valmaro.adapter.OrderAdapter
import com.example.valmaro.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class OrderFragment : Fragment() {

    private lateinit var ordersRecyclerView: RecyclerView

    private val firebaseOrders = mutableListOf<Order>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_order,
            container,
            false
        )

        ordersRecyclerView =
            view.findViewById(R.id.ordersRecyclerView)

        ordersRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        loadOrders()

        return view
    }

    private fun loadOrders() {

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Toast.makeText(
                requireContext(),
                "Please login first",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val uid = user.uid

        val ordersRef =
            FirebaseDatabase.getInstance()
                .getReference("orders")
                .child(uid)

        ordersRef.addValueEventListener(
            object : ValueEventListener {

                override fun onDataChange(
                    snapshot: DataSnapshot
                ) {

                    firebaseOrders.clear()

                    for (orderSnapshot in snapshot.children) {

                        val order =
                            orderSnapshot.getValue(
                                Order::class.java
                            )

                        if (order != null) {
                            firebaseOrders.add(order)
                        }
                    }

                    ordersRecyclerView.adapter =
                        OrderAdapter(firebaseOrders)
                }

                override fun onCancelled(
                    error: DatabaseError
                ) {

                    Toast.makeText(
                        requireContext(),
                        "Firebase Error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}