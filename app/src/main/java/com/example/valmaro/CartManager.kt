package com.example.valmaro

import android.content.Context
import com.example.valmaro.model.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class CartItem(
    val product: Product,
    var quantity: Int = 1
)

object CartManager {

    val cartItems = mutableListOf<CartItem>()

    private const val PREF_NAME = "ValmaroCart"
    private const val CART_KEY = "cartItems"

    private lateinit var prefs: android.content.SharedPreferences

    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )
        loadCart()
    }

    private fun saveCart() {
        if (!::prefs.isInitialized) return

        val json = gson.toJson(cartItems)

        prefs.edit()
            .putString(CART_KEY, json)
            .apply()
    }

    private fun loadCart() {
        if (!::prefs.isInitialized) return

        val json = prefs.getString(CART_KEY, null) ?: return

        val type = object : TypeToken<MutableList<CartItem>>() {}.type

        val savedItems: MutableList<CartItem> =
            try {
                gson.fromJson(json, type) ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }

        cartItems.clear()
        cartItems.addAll(savedItems)
    }

    fun addToCart(product: Product) {

        val existingItem = cartItems.find {
            it.product.productId == product.productId
        }

        if (existingItem != null) {
            existingItem.quantity++
        } else {
            cartItems.add(
                CartItem(
                    product = product,
                    quantity = 1
                )
            )
        }

        saveCart()
    }

    fun removeFromCart(product: Product) {

        val existingItem = cartItems.find {
            it.product.productId == product.productId
        }

        if (existingItem != null) {

            if (existingItem.quantity > 1) {
                existingItem.quantity--
            } else {
                cartItems.remove(existingItem)
            }
        }

        saveCart()
    }

    fun removeCartItem(item: CartItem) {

        cartItems.remove(item)

        saveCart()
    }

    fun clearCart() {

        cartItems.clear()

        saveCart()
    }

    // Checkout ke baad updated cart save karne ke liye
    fun saveCartFromCheckout() {
        saveCart()
    }

    fun getTotal(): Int {

        return cartItems.sumOf { item ->

            val product = item.product

            val finalPrice =
                if (product.discountPrice.isNotBlank()) {

                    product.discountPrice
                        .replace("₹", "")
                        .replace(",", "")
                        .toIntOrNull() ?: 0

                } else {

                    product.price
                        .replace("₹", "")
                        .replace(",", "")
                        .toIntOrNull() ?: 0
                }

            finalPrice * item.quantity
        }
    }
}