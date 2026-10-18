package com.example.valmaro.model

data class Order(
    val orderId: String = "",
    val productName: String = "",
    val price: String = "",
    val quantity: Int = 0,

    // Order Status
    var status: String = "Confirmed",

    val address: String = "",
    val customerName: String = "",
    val phone: String = "",

    // Order Date
    val orderDate: Long = 0L,

    // Estimated Delivery Date
    val estimatedDeliveryDate: Long = 0L,

    val paymentStatus: String = "COD",
    val paymentId: String = "",
    val razorpayOrderId: String = ""
)