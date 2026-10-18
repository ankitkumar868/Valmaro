package com.example.valmaro

import com.example.valmaro.model.Product

data class ChatMessage(
    val message: String,
    val isUser: Boolean,
    val product: Product? = null
)