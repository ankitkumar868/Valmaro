package com.example.valmaro.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.valmaro.R
import com.example.valmaro.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AdminOrderAdapter(
    private val orders: MutableList<Order>
) : RecyclerView.Adapter<AdminOrderAdapter.AdminOrderViewHolder>() {

    class AdminOrderViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val productName: TextView =
            itemView.findViewById(R.id.adminProductName)

        val orderId: TextView =
            itemView.findViewById(R.id.adminOrderId)

        val customerName: TextView =
            itemView.findViewById(R.id.adminCustomerName)

        val phone: TextView =
            itemView.findViewById(R.id.adminPhone)

        val address: TextView =
            itemView.findViewById(R.id.adminAddress)

        val quantity: TextView =
            itemView.findViewById(R.id.adminQuantity)

        val price: TextView =
            itemView.findViewById(R.id.adminPrice)

        val currentStatus: TextView =
            itemView.findViewById(R.id.adminCurrentStatus)

        val statusSpinner: Spinner =
            itemView.findViewById(R.id.adminStatusSpinner)

        val updateButton: Button =
            itemView.findViewById(R.id.updateStatusButton)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdminOrderViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_admin_order,
                parent,
                false
            )

        return AdminOrderViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: AdminOrderViewHolder,
        position: Int
    ) {

        val order = orders[position]

        holder.productName.text =
            order.productName

        holder.orderId.text =
            "Order ID: ${order.orderId}"

        holder.customerName.text =
            "Customer: ${order.customerName}"

        holder.phone.text =
            "Phone: ${order.phone}"

        holder.address.text =
            "Address: ${order.address}"

        holder.quantity.text =
            "Quantity: ${order.quantity}"

        holder.price.text =
            "Price: ${order.price}"

        holder.currentStatus.text =
            "Status: ${order.status}"

        setStatusColor(
            holder.currentStatus,
            order.status
        )

        val statusList = arrayOf(
            "Confirmed",
            "Shipped",
            "Out for Delivery",
            "Delivered"
        )

        val spinnerAdapter = ArrayAdapter(
            holder.itemView.context,
            android.R.layout.simple_spinner_dropdown_item,
            statusList
        )

        holder.statusSpinner.adapter = spinnerAdapter

        val currentIndex =
            statusList.indexOf(order.status)

        if (currentIndex >= 0) {
            holder.statusSpinner.setSelection(currentIndex)
        }

        holder.updateButton.setOnClickListener {

            val newStatus =
                holder.statusSpinner.selectedItem.toString()

            if (newStatus == order.status) {

                Toast.makeText(
                    holder.itemView.context,
                    "Select a different status",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            updateOrderStatus(
                holder,
                order,
                newStatus
            )
        }
    }

    private fun updateOrderStatus(
        holder: AdminOrderViewHolder,
        order: Order,
        newStatus: String
    ) {

        val currentUser =
            FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {

            Toast.makeText(
                holder.itemView.context,
                "Admin login required",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        holder.updateButton.isEnabled = false

        val ordersRef =
            FirebaseDatabase.getInstance()
                .getReference("orders")

        ordersRef.get()
            .addOnSuccessListener { snapshot ->

                var found = false

                for (userSnapshot in snapshot.children) {

                    val uid =
                        userSnapshot.key ?: continue

                    val orderSnapshot =
                        userSnapshot
                            .child(order.orderId)

                    if (orderSnapshot.exists()) {

                        found = true

                        val statusRef =
                            ordersRef
                                .child(uid)
                                .child(order.orderId)
                                .child("status")

                        statusRef
                            .setValue(newStatus)
                            .addOnSuccessListener {

                                order.status = newStatus

                                holder.currentStatus.text =
                                    "Status: $newStatus"

                                setStatusColor(
                                    holder.currentStatus,
                                    newStatus
                                )

                                holder.updateButton.isEnabled =
                                    true

                                Toast.makeText(
                                    holder.itemView.context,
                                    "Status updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { error ->

                                holder.updateButton.isEnabled =
                                    true

                                Toast.makeText(
                                    holder.itemView.context,
                                    "Update failed: ${error.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        break
                    }
                }

                if (!found) {

                    holder.updateButton.isEnabled = true

                    Toast.makeText(
                        holder.itemView.context,
                        "Order not found",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener { error ->

                holder.updateButton.isEnabled = true

                Toast.makeText(
                    holder.itemView.context,
                    "Firebase error: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun setStatusColor(
        textView: TextView,
        status: String
    ) {

        when (status) {

            "Confirmed" -> {
                textView.setTextColor(
                    Color.parseColor("#2E7D32")
                )
            }

            "Shipped" -> {
                textView.setTextColor(
                    Color.parseColor("#1565C0")
                )
            }

            "Out for Delivery" -> {
                textView.setTextColor(
                    Color.parseColor("#EF6C00")
                )
            }

            "Delivered" -> {
                textView.setTextColor(
                    Color.parseColor("#00897B")
                )
            }

            else -> {
                textView.setTextColor(Color.DKGRAY)
            }
        }
    }

    override fun getItemCount(): Int {
        return orders.size
    }
}