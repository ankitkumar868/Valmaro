package com.example.valmaro.model

data class Product(
    val name: String,
    val price: String,
    val rating: String,
    val image: Int,
    val description: String,

    // Firebase data
    val category: String = "",
    val stock: Int = 0,
    val sales: Int = 0,
    val views: Int = 0,

    // Firebase Product ID
    val productId: String = "",

    // Firebase Storage Image URL
    val imageUrl: String = "",
    // Discount Details
    val discountPrice: String = "",
    val discountPercent: Int = 0
)