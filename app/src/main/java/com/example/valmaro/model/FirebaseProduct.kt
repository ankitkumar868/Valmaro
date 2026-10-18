package com.example.valmaro.model

data class FirebaseProduct(
    val imageUrl: String = "",
    val name: String = "",
    val price: Any? = 0.0,
    val rating: Any? = 0.0,
    val category: String = "",
    val description: String = "",
    val stock: Int = 0,
    val sales: Int = 0,
    val views: Int = 0,

    // Discount Details
    val discountPrice: Any? = null,
    val discountPercent: Int = 0
)