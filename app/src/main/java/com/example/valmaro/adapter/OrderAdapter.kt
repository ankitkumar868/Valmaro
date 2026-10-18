package com.example.valmaro.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.valmaro.R
import com.example.valmaro.model.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderAdapter(
    private val orders: List<Order>
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val orderProductName: TextView =
            itemView.findViewById(R.id.orderProductName)

        val orderPrice: TextView =
            itemView.findViewById(R.id.orderPrice)

        val orderQuantity: TextView =
            itemView.findViewById(R.id.orderQuantity)

        val orderStatus: TextView =
            itemView.findViewById(R.id.orderStatus)

        val orderAddress: TextView =
            itemView.findViewById(R.id.orderAddress)

        val orderId: TextView =
            itemView.findViewById(R.id.orderId)

        val orderDate: TextView =
            itemView.findViewById(R.id.orderDate)

        // New Delivery Date TextView
        val orderDeliveryDate: TextView =
            itemView.findViewById(R.id.orderDeliveryDate)

        val orderCustomerName: TextView =
            itemView.findViewById(R.id.orderCustomerName)

        val orderPhone: TextView =
            itemView.findViewById(R.id.orderPhone)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OrderViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_order,
                parent,
                false
            )

        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: OrderViewHolder,
        position: Int
    ) {

        val order = orders[position]

        holder.orderProductName.text =
            order.productName

        holder.orderPrice.text =
            "Price: ${order.price}"

        holder.orderQuantity.text =
            "Quantity: ${order.quantity}"

        when (order.status) {

            "Confirmed" -> {
                holder.orderStatus.text = "Status: Confirmed"
                holder.orderStatus.setTextColor(
                    android.graphics.Color.parseColor("#2E7D32")
                )
            }

            "Shipped" -> {
                holder.orderStatus.text = "Status: Shipped"
                holder.orderStatus.setTextColor(
                    android.graphics.Color.parseColor("#1565C0")
                )
            }

            "Out for Delivery" -> {
                holder.orderStatus.text = "Status: Out for Delivery"
                holder.orderStatus.setTextColor(
                    android.graphics.Color.parseColor("#EF6C00")
                )
            }

            "Delivered" -> {
                holder.orderStatus.text = "Status: Delivered"
                holder.orderStatus.setTextColor(
                    android.graphics.Color.parseColor("#00897B")
                )
            }

            else -> {
                holder.orderStatus.text =
                    "Status: ${order.status}"

                holder.orderStatus.setTextColor(
                    android.graphics.Color.DKGRAY
                )
            }
        }

        holder.orderAddress.text =
            "Address: ${order.address}"

        holder.orderId.text =
            "Order ID: ${order.orderId}"

        holder.orderCustomerName.text =
            "Customer: ${order.customerName}"

        holder.orderPhone.text =
            "Phone: ${order.phone}"

        // Order Date
        if (order.orderDate > 0) {

            val dateFormat = SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
            )

            holder.orderDate.text =
                "Ordered on: ${dateFormat.format(Date(order.orderDate))}"

        } else {

            holder.orderDate.text = "Ordered on: --"
        }

        // Estimated Delivery Date
        if (order.estimatedDeliveryDate > 0) {

            val deliveryFormat = SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
            )

            holder.orderDeliveryDate.text =
                "Expected Delivery: ${
                    deliveryFormat.format(
                        Date(order.estimatedDeliveryDate)
                    )
                }"

        } else {

            holder.orderDeliveryDate.text =
                "Expected Delivery: Not available"
        }
    }

    override fun getItemCount(): Int {
        return orders.size
    }
}