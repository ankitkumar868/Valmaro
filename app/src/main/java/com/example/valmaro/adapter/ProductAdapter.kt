package com.example.valmaro.adapter

import android.content.Intent
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.valmaro.manager.WatchlistManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.valmaro.CartManager
import com.example.valmaro.ProductDetailsActivity
import com.example.valmaro.R
import com.example.valmaro.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProductAdapter(
    private val products: List<Product>
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val image: ImageView =
            itemView.findViewById(R.id.productImage)

        val name: TextView =
            itemView.findViewById(R.id.productName)

        val rating: TextView =
            itemView.findViewById(R.id.productRating)

        val price: TextView =
            itemView.findViewById(R.id.txtProductPrice)

        val originalPrice: TextView =
            itemView.findViewById(R.id.txtOriginalPrice)

        val discountPrice: TextView =
            itemView.findViewById(R.id.txtDiscountPrice)

        val discountBadge: TextView =
            itemView.findViewById(R.id.txtDiscountBadge)

        val addToCart: Button =
            itemView.findViewById(R.id.addToCartButton)

        val watchlistButton: Button =
            itemView.findViewById(R.id.btnWatchlist)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)

        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ProductViewHolder,
        position: Int
    ) {

        val product = products[position]
        val context = holder.itemView.context

        // =========================
        // PRODUCT IMAGE
        // =========================

        if (product.imageUrl.isNotBlank()) {

            Glide.with(context)
                .load(product.imageUrl)
                .placeholder(product.image)
                .error(product.image)
                .into(holder.image)

        } else {
            holder.image.setImageResource(product.image)
        }

        // =========================
        // NAME AND RATING
        // =========================

        holder.name.text = product.name
        holder.rating.text = "⭐ ${product.rating}"

        // =========================
        // PRICE AND DISCOUNT
        // =========================

        val originalPriceValue = product.price
            .replace("₹", "")
            .replace(",", "")
            .trim()
            .toDoubleOrNull() ?: 0.0

        val discountedPriceValue = product.discountPrice
            .replace("₹", "")
            .replace(",", "")
            .trim()
            .toDoubleOrNull() ?: 0.0

        val hasDiscount =
            discountedPriceValue > 0 &&
                    originalPriceValue > 0 &&
                    discountedPriceValue < originalPriceValue &&
                    product.discountPercent > 0

        if (hasDiscount) {

            holder.originalPrice.visibility = View.VISIBLE
            holder.originalPrice.text = product.price

            holder.originalPrice.paintFlags =
                holder.originalPrice.paintFlags or
                        Paint.STRIKE_THRU_TEXT_FLAG

            holder.discountPrice.visibility = View.VISIBLE
            holder.discountPrice.text = product.discountPrice

            holder.discountBadge.visibility = View.VISIBLE
            holder.discountBadge.text =
                "${product.discountPercent}% OFF"

            holder.price.visibility = View.GONE

        } else {

            holder.price.visibility = View.VISIBLE
            holder.price.text = product.price

            holder.originalPrice.visibility = View.GONE
            holder.discountPrice.visibility = View.GONE
            holder.discountBadge.visibility = View.GONE

            holder.originalPrice.paintFlags =
                holder.originalPrice.paintFlags and
                        Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        // =========================
        // WATCHLIST / HEART BUTTON
        // =========================

        // =========================
// WATCHLIST / HEART BUTTON
// =========================

        val productId = product.productId

// Default Heart
        holder.watchlistButton.text = "♡"

// Check if product is already in Watchlist
        WatchlistManager.isInWatchlist(
            productId = productId,
            onResult = { isAdded ->

                val currentPosition = holder.adapterPosition

                if (currentPosition != RecyclerView.NO_POSITION &&
                    products.getOrNull(currentPosition)?.productId == productId
                ) {
                    holder.watchlistButton.text =
                        if (isAdded) "♥" else "♡"
                }
            },
            onError = {
                // Keep default heart if check fails
            }
        )

// Heart Button Click
        holder.watchlistButton.setOnClickListener {

            if (productId.isBlank()) {
                Toast.makeText(
                    context,
                    "Product ID is missing",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            WatchlistManager.isInWatchlist(
                productId = productId,
                onResult = { isAdded ->

                    if (isAdded) {

                        // REMOVE FROM WATCHLIST
                        WatchlistManager.removeFromWatchlist(
                            productId = productId,
                            onSuccess = {

                                holder.watchlistButton.text = "♡"

                                Toast.makeText(
                                    context,
                                    "Removed from Watchlist",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onError = { error ->
                                Toast.makeText(
                                    context,
                                    error,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )

                    } else {

                        // ADD TO WATCHLIST
                        WatchlistManager.addToWatchlist(
                            productId = productId,
                            onSuccess = {

                                holder.watchlistButton.text = "♥"

                                Toast.makeText(
                                    context,
                                    "Added to Watchlist ❤️",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onError = { error ->
                                Toast.makeText(
                                    context,
                                    error,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                },
                onError = { error ->
                    Toast.makeText(
                        context,
                        error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        // =========================
        // ADD TO CART
        // =========================

        if (product.stock <= 0) {

            holder.addToCart.text = "OUT OF STOCK"
            holder.addToCart.isEnabled = false
            holder.addToCart.setOnClickListener(null)

        } else {

            holder.addToCart.text = "ADD TO CART"
            holder.addToCart.isEnabled = true

            holder.addToCart.setOnClickListener {

                CartManager.addToCart(product)

                Toast.makeText(
                    context,
                    "Added to cart",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // =========================
        // PRODUCT DETAILS
        // =========================

        holder.itemView.setOnClickListener {

            val intent = Intent(
                context,
                ProductDetailsActivity::class.java
            )

            intent.putExtra("product_image_url", product.imageUrl)
            intent.putExtra("product_name", product.name)
            intent.putExtra("product_price", product.price)
            intent.putExtra("product_rating", product.rating)
            intent.putExtra("product_description", product.description)
            intent.putExtra("product_category", product.category)
            intent.putExtra("product_stock", product.stock)
            intent.putExtra("product_image", product.image)
            intent.putExtra("product_id", product.productId)
            intent.putExtra("product_discount_price", product.discountPrice)
            intent.putExtra(
                "product_discount_percent",
                product.discountPercent
            )

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return products.size
    }
}