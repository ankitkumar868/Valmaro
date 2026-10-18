package com.example.valmaro.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.valmaro.CartItem
import com.example.valmaro.R
import com.example.valmaro.model.Product

class CartAdapter(
    private val cartItems: MutableList<CartItem>,
    private val stockMap: MutableMap<String, Int>,
    private val onCartChanged: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val image: ImageView =
            itemView.findViewById(R.id.cartProductImage)

        val name: TextView =
            itemView.findViewById(R.id.cartProductName)

        val price: TextView =
            itemView.findViewById(R.id.cartProductPrice)

        val quantity: TextView =
            itemView.findViewById(R.id.cartQuantity)

        val minus: Button =
            itemView.findViewById(R.id.minusButton)

        val plus: Button =
            itemView.findViewById(R.id.plusButton)

        val stockStatus: TextView =
            itemView.findViewById(R.id.cartStockStatus)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CartViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)

        return CartViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CartViewHolder,
        position: Int
    ) {

        val item = cartItems[position]
        val product = item.product

        // -----------------------------
        // PRODUCT IMAGE
        // -----------------------------
        if (product.imageUrl.isNotBlank()) {

            Glide.with(holder.itemView.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(product.image)
                .into(holder.image)

        } else {

            holder.image.setImageResource(product.image)
        }

        // -----------------------------
        // PRODUCT NAME
        // -----------------------------
        holder.name.text = product.name

        // -----------------------------
        // DISCOUNTED PRICE
        // -----------------------------
        holder.price.text =
            if (product.discountPrice.isNotBlank()) {
                product.discountPrice
            } else {
                product.price
            }

        // -----------------------------
        // QUANTITY
        // -----------------------------
        holder.quantity.text =
            item.quantity.toString()

        // -----------------------------
        // CURRENT STOCK
        // -----------------------------
        val currentStock =
            getStock(product)

        // -----------------------------
        // STOCK STATUS
        // -----------------------------
        if (currentStock <= 0) {

            holder.stockStatus.visibility =
                View.VISIBLE

            holder.stockStatus.text =
                "OUT OF STOCK"

            holder.plus.isEnabled =
                false

        } else {

            holder.stockStatus.visibility =
                View.GONE

            holder.plus.isEnabled =
                item.quantity < currentStock
        }

        // -----------------------------
        // PLUS BUTTON
        // -----------------------------
        holder.plus.setOnClickListener {

            val latestStock =
                getStock(product)

            if (latestStock <= 0) {

                Toast.makeText(
                    holder.itemView.context,
                    "OUT OF STOCK",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (item.quantity < latestStock) {

                item.quantity++

                holder.quantity.text =
                    item.quantity.toString()

                holder.plus.isEnabled =
                    item.quantity < latestStock

                onCartChanged()

            } else {

                Toast.makeText(
                    holder.itemView.context,
                    "Only $latestStock item(s) available",
                    Toast.LENGTH_SHORT
                ).show()

                holder.plus.isEnabled =
                    false
            }
        }

        // -----------------------------
        // MINUS BUTTON
        // -----------------------------
        holder.minus.setOnClickListener {

            if (item.quantity > 1) {

                item.quantity--

                holder.quantity.text =
                    item.quantity.toString()

                holder.plus.isEnabled =
                    currentStock > item.quantity

                onCartChanged()

            } else {

                if (position >= 0 &&
                    position < cartItems.size
                ) {

                    cartItems.removeAt(position)

                    notifyItemRemoved(position)

                    onCartChanged()
                }
            }
        }
    }

    // -----------------------------
    // GET STOCK
    // -----------------------------
    private fun getStock(product: Product): Int {

        // First: Product ID
        if (product.productId.isNotBlank()) {

            val stockById =
                stockMap[product.productId]

            if (stockById != null) {
                return stockById
            }
        }

        // Second: Product Name
        val stockByName =
            stockMap[product.name]

        if (stockByName != null) {
            return stockByName
        }

        // No stock found
        return 0
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }
}