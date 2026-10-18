package com.example.valmaro.model

data class WatchlistItem(
    val productId: String = "",
    val name: String = "",
    val category: String = "",
    val price: String = "",
    val discountPrice: String = "",
    val discountPercent: Int = 0,
    val imageUrl: String = "",
    val rating: String = "",
    val description: String = ""
)