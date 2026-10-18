package com.example.valmaro

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.valmaro.adapter.ProductAdapter
import com.example.valmaro.model.FirebaseProduct
import com.example.valmaro.model.Product
import com.example.valmaro.utils.PricePoint
import com.example.valmaro.utils.PricePrediction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProductDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        // =========================
        // FIND VIEWS
        // =========================

        val productImage = findViewById<ImageView>(
            R.id.detailProductImage
        )

        val productName = findViewById<TextView>(
            R.id.detailProductName
        )

        val productPrice = findViewById<TextView>(
            R.id.productPrice
        )

        val productRating = findViewById<TextView>(
            R.id.detailProductRating
        )

        val productDescription = findViewById<TextView>(
            R.id.detailProductDescription
        )

        val productCategory = findViewById<TextView>(
            R.id.detailProductCategory
        )

        val productStock = findViewById<TextView>(
            R.id.detailProductStock
        )

        val addToCart = findViewById<Button>(
            R.id.detailAddToCart
        )

        val buyNow = findViewById<Button>(
            R.id.detailBuyNow
        )

        // Discount views
        val originalPriceView = findViewById<TextView>(
            R.id.detailOriginalPrice
        )

        val discountPriceView = findViewById<TextView>(
            R.id.detailDiscountPrice
        )

        val discountBadgeView = findViewById<TextView>(
            R.id.detailDiscountBadge
        )

        val youSaveView = findViewById<TextView>(
            R.id.detailYouSave
        )

        // Price prediction views
        val predictedPriceText = findViewById<TextView>(
            R.id.predictedPriceText
        )

        val predictedSavingsText = findViewById<TextView>(
            R.id.predictedSavingsText
        )

        val priceTrendText = findViewById<TextView>(
            R.id.priceTrendText
        )

        // Rating views
        val customerRatingBar = findViewById<RatingBar>(
            R.id.customerRatingBar
        )

        val submitRatingButton = findViewById<Button>(
            R.id.submitRatingButton
        )

        // =========================
        // GET PRODUCT DATA
        // =========================

        val name =
            intent.getStringExtra("product_name") ?: ""

        val price =
            intent.getStringExtra("product_price") ?: ""

        val rating =
            intent.getStringExtra("product_rating") ?: ""

        val description =
            intent.getStringExtra("product_description") ?: ""

        val category =
            intent.getStringExtra("product_category") ?: ""

        val stock =
            intent.getIntExtra("product_stock", 0)

        val productId =
            intent.getStringExtra("product_id") ?: ""

        val image =
            intent.getIntExtra("product_image", 0)

        val imageUrl =
            intent.getStringExtra("product_image_url") ?: ""

        val discountPrice =
            intent.getStringExtra("product_discount_price") ?: ""

        val discountPercent =
            intent.getIntExtra(
                "product_discount_percent",
                0
            )

        // =========================
        // LOAD RELATED PRODUCTS
        // =========================

        loadRelatedProducts(
            category,
            productId
        )

        // =========================
        // PRICE PREDICTION
        // =========================

        val currentPrice = price
            .replace("₹", "")
            .replace(",", "")
            .trim()
            .toDoubleOrNull() ?: 0.0

        if (productId.isNotEmpty() && currentPrice > 0) {

            loadPricePrediction(
                productId,
                currentPrice,
                predictedPriceText,
                predictedSavingsText,
                priceTrendText
            )

        } else {

            predictedPriceText.text =
                "Prediction unavailable"

            predictedSavingsText.text =
                "Price data not available"

            priceTrendText.text =
                "Unable to analyze price trend"
        }

        // =========================
        // SHOW PRODUCT INFORMATION
        // =========================

        productName.text = name
        productRating.text = "⭐ $rating"
        productDescription.text = description
        productCategory.text = "Category: $category"
        productStock.text = "Stock: $stock"

        // =========================
        // SHOW PRODUCT DISCOUNT
        // =========================

        val originalPriceValue = price
            .replace("₹", "")
            .replace(",", "")
            .trim()
            .toDoubleOrNull() ?: 0.0

        val discountedPriceValue = discountPrice
            .replace("₹", "")
            .replace(",", "")
            .trim()
            .toDoubleOrNull() ?: 0.0

        val hasDiscount =
            discountedPriceValue > 0 &&
                    discountedPriceValue < originalPriceValue &&
                    discountPercent > 0

        if (hasDiscount) {

            originalPriceView.visibility =
                View.VISIBLE

            originalPriceView.text =
                price

            originalPriceView.paintFlags =
                originalPriceView.paintFlags or
                        Paint.STRIKE_THRU_TEXT_FLAG

            discountPriceView.visibility =
                View.VISIBLE

            discountPriceView.text =
                discountPrice

            discountBadgeView.visibility =
                View.VISIBLE

            discountBadgeView.text =
                "$discountPercent% OFF"

            val savings =
                originalPriceValue -
                        discountedPriceValue

            youSaveView.visibility =
                View.VISIBLE

            youSaveView.text =
                "You Save ₹${savings.toInt()}"

            productPrice.visibility =
                View.GONE

        } else {

            productPrice.visibility =
                View.VISIBLE

            productPrice.text =
                price

            originalPriceView.visibility =
                View.GONE

            discountPriceView.visibility =
                View.GONE

            discountBadgeView.visibility =
                View.GONE

            youSaveView.visibility =
                View.GONE
        }

        // =========================
        // LOAD PRODUCT IMAGE
        // =========================

        if (imageUrl.isNotBlank()) {

            Glide.with(this)
                .load(imageUrl)
                .placeholder(image)
                .error(image)
                .into(productImage)

        } else if (image != 0) {

            productImage.setImageResource(image)
        }

        // =========================
        // CREATE PRODUCT OBJECT
        // =========================

        val product = Product(
            name = name,
            price = price,
            rating = rating,
            image = image,
            description = description,
            category = category,
            stock = stock,
            productId = productId,
            imageUrl = imageUrl,
            discountPrice = discountPrice,
            discountPercent = discountPercent
        )

        // =========================
        // ADD TO CART
        // =========================

        addToCart.setOnClickListener {

            if (stock <= 0) {

                Toast.makeText(
                    this,
                    "OUT OF STOCK",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            CartManager.addToCart(product)

            Toast.makeText(
                this,
                "$name added to cart",
                Toast.LENGTH_SHORT
            ).show()
        }

        // =========================
        // CUSTOMER RATING
        // =========================

        submitRatingButton.setOnClickListener {

            val user =
                FirebaseAuth.getInstance().currentUser

            if (user == null) {

                Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (productId.isEmpty()) {

                Toast.makeText(
                    this,
                    "Product ID not found",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val selectedRating =
                customerRatingBar.rating.toInt()

            if (selectedRating !in 1..5) {

                Toast.makeText(
                    this,
                    "Please select a rating from 1 to 5",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val productRef =
                FirebaseDatabase.getInstance()
                    .getReference("products")
                    .child(productId)

            val ratingsRef =
                productRef.child("ratings")

            submitRatingButton.isEnabled =
                false

            ratingsRef.child(user.uid)
                .setValue(selectedRating)
                .addOnSuccessListener {

                    ratingsRef.get()
                        .addOnSuccessListener { snapshot ->

                            var total = 0
                            var count = 0

                            for (ratingSnapshot in snapshot.children) {

                                val value =
                                    ratingSnapshot
                                        .getValue(Int::class.java)

                                if (value != null) {

                                    total += value
                                    count++
                                }
                            }

                            val average =
                                if (count > 0) {
                                    total.toDouble() / count
                                } else {
                                    0.0
                                }

                            val updates =
                                mapOf<String, Any>(
                                    "rating" to average,
                                    "totalReviews" to count
                                )

                            productRef
                                .updateChildren(updates)
                                .addOnSuccessListener {

                                    productRating.text =
                                        "⭐ %.1f (%d reviews)"
                                            .format(
                                                average,
                                                count
                                            )

                                    Toast.makeText(
                                        this,
                                        "Rating submitted successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    submitRatingButton.isEnabled =
                                        true
                                }
                                .addOnFailureListener {

                                    submitRatingButton.isEnabled =
                                        true

                                    Toast.makeText(
                                        this,
                                        "Failed to update average rating",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        .addOnFailureListener {

                            submitRatingButton.isEnabled =
                                true

                            Toast.makeText(
                                this,
                                "Unable to load ratings",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .addOnFailureListener { error ->

                    submitRatingButton.isEnabled =
                        true

                    Toast.makeText(
                        this,
                        "Rating failed: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        // =========================
        // BUY NOW
        // =========================

        buyNow.setOnClickListener {

            // Stock check
            if (stock <= 0) {

                Toast.makeText(
                    this,
                    "OUT OF STOCK",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // IMPORTANT:
            // Buy Now product ko Cart mein add nahi karna.
            // Direct CheckoutActivity ko product bhejna hai.

            val checkoutIntent =
                Intent(
                    this,
                    CheckoutActivity::class.java
                )

            checkoutIntent.putExtra(
                "buy_now_mode",
                true
            )

            checkoutIntent.putExtra(
                "product_name",
                product.name
            )

            checkoutIntent.putExtra(
                "product_price",
                product.price
            )

            checkoutIntent.putExtra(
                "product_rating",
                product.rating
            )

            checkoutIntent.putExtra(
                "product_description",
                product.description
            )

            checkoutIntent.putExtra(
                "product_category",
                product.category
            )

            checkoutIntent.putExtra(
                "product_stock",
                product.stock
            )

            checkoutIntent.putExtra(
                "product_id",
                product.productId
            )

            checkoutIntent.putExtra(
                "product_image",
                product.image
            )

            checkoutIntent.putExtra(
                "product_image_url",
                product.imageUrl
            )

            checkoutIntent.putExtra(
                "product_discount_price",
                product.discountPrice
            )

            checkoutIntent.putExtra(
                "product_discount_percent",
                product.discountPercent
            )

            startActivity(checkoutIntent)
        }
    }

    // ==========================================
    // PRICE PREDICTION
    // ==========================================

    private fun loadPricePrediction(
        productId: String,
        currentPrice: Double,
        predictedPriceText: TextView,
        predictedSavingsText: TextView,
        priceTrendText: TextView
    ) {

        val historyRef =
            FirebaseDatabase.getInstance()
                .getReference("products")
                .child(productId)
                .child("priceHistory")

        historyRef.get()
            .addOnSuccessListener { snapshot ->

                val history =
                    mutableListOf<PricePoint>()

                for (item in snapshot.children) {

                    val priceValue =
                        item.child("price")
                            .getValue(Double::class.java)

                    val date =
                        item.child("date")
                            .getValue(String::class.java)

                    if (
                        priceValue != null &&
                        date != null &&
                        priceValue > 0
                    ) {

                        history.add(
                            PricePoint(
                                price = priceValue,
                                date = date
                            )
                        )
                    }
                }

                if (history.size < 2) {

                    predictedPriceText.text =
                        "Not enough data"

                    predictedSavingsText.text =
                        "More price history is needed"

                    priceTrendText.text =
                        "At least 2 price records are required"

                    return@addOnSuccessListener
                }

                val predictedPrice =
                    PricePrediction.predictNext7Days(
                        history
                    )

                val formattedPrice =
                    "₹${"%.0f".format(
                        java.util.Locale.US,
                        predictedPrice
                    )}"

                predictedPriceText.text =
                    formattedPrice

                val savings =
                    currentPrice - predictedPrice

                when {

                    savings > 0 -> {

                        predictedSavingsText.text =
                            "Potential savings: ₹${"%.0f".format(savings)}"

                        priceTrendText.text =
                            "📉 Price may decrease"
                    }

                    savings < 0 -> {

                        predictedSavingsText.text =
                            "Expected increase: ₹${"%.0f".format(-savings)}"

                        predictedSavingsText.setTextColor(
                            android.graphics.Color.RED
                        )

                        priceTrendText.text =
                            "📈 Price may increase"
                    }

                    else -> {

                        predictedSavingsText.text =
                            "No expected price change"

                        priceTrendText.text =
                            "➡️ Price may remain stable"
                    }
                }
            }
            .addOnFailureListener { error ->

                predictedPriceText.text =
                    "Prediction unavailable"

                predictedSavingsText.text =
                    "Unable to load price history"

                priceTrendText.text =
                    error.message
                        ?: "Please try again later"
            }
    }

    // ==========================================
    // RELATED PRODUCTS
    // ==========================================

    private fun loadRelatedProducts(
        currentCategory: String,
        currentProductId: String
    ) {

        val relatedRecyclerView =
            findViewById<RecyclerView>(
                R.id.relatedProductsRecyclerView
            )

        val relatedTitle =
            findViewById<TextView>(
                R.id.relatedProductsTitle
            )

        relatedRecyclerView.layoutManager =
            GridLayoutManager(this, 2)

        relatedRecyclerView.isNestedScrollingEnabled =
            false

        FirebaseDatabase.getInstance()
            .getReference("products")
            .addListenerForSingleValueEvent(
                object : ValueEventListener {

                    override fun onDataChange(
                        snapshot: DataSnapshot
                    ) {

                        val relatedProducts =
                            mutableListOf<Product>()

                        Log.d(
                            "RELATED",
                            "Current Category: $currentCategory"
                        )

                        Log.d(
                            "RELATED",
                            "Current Product ID: $currentProductId"
                        )

                        for (
                        productSnapshot
                        in snapshot.children
                        ) {

                            val productId =
                                productSnapshot.key
                                    ?: continue

                            if (
                                productId ==
                                currentProductId
                            ) {
                                continue
                            }

                            val rawValue =
                                productSnapshot.value

                            if (rawValue !is Map<*, *>) {

                                Log.w(
                                    "RELATED",
                                    "Skipping invalid product: $productId"
                                )

                                continue
                            }

                            val firebaseProduct =
                                try {

                                    productSnapshot.getValue(
                                        FirebaseProduct::class.java
                                    ) ?: continue

                                } catch (e: Exception) {

                                    Log.e(
                                        "RELATED",
                                        "Failed to parse product: $productId",
                                        e
                                    )

                                    continue
                                }

                            if (
                                !firebaseProduct.category
                                    .trim()
                                    .equals(
                                        currentCategory.trim(),
                                        ignoreCase = true
                                    )
                            ) {

                                continue
                            }

                            val productImage =
                                when {

                                    firebaseProduct.category
                                        .trim()
                                        .equals(
                                            "Shoes",
                                            ignoreCase = true
                                        ) ->
                                        R.drawable.shoes

                                    firebaseProduct.category
                                        .trim()
                                        .equals(
                                            "Clothes",
                                            ignoreCase = true
                                        ) ->
                                        R.drawable.tshirt

                                    firebaseProduct.category
                                        .trim()
                                        .equals(
                                            "Watches",
                                            ignoreCase = true
                                        ) ->
                                        R.drawable.watch

                                    else ->
                                        R.drawable.shoes
                                }

                            val relatedPrice =
                                firebaseProduct.price
                                    .toString()
                                    .toDoubleOrNull()
                                    ?: 0.0

                            val relatedDiscountPrice =
                                firebaseProduct.discountPrice
                                    ?.toString()
                                    ?.toDoubleOrNull()
                                    ?: 0.0

                            val relatedProduct =
                                Product(
                                    name =
                                        firebaseProduct.name,

                                    price =
                                        "₹${relatedPrice.toInt()}",

                                    rating =
                                        firebaseProduct.rating
                                            .toString(),

                                    image =
                                        productImage,

                                    description =
                                        firebaseProduct.description,

                                    category =
                                        firebaseProduct.category,

                                    stock =
                                        firebaseProduct.stock,

                                    sales =
                                        firebaseProduct.sales,

                                    views =
                                        firebaseProduct.views,

                                    productId =
                                        productId,

                                    imageUrl =
                                        firebaseProduct.imageUrl,

                                    discountPrice =
                                        if (
                                            relatedDiscountPrice > 0 &&
                                            relatedDiscountPrice < relatedPrice
                                        ) {
                                            "₹${relatedDiscountPrice.toInt()}"
                                        } else {
                                            ""
                                        },

                                    discountPercent =
                                        firebaseProduct.discountPercent
                                )

                            relatedProducts.add(
                                relatedProduct
                            )
                        }

                        if (
                            relatedProducts.isEmpty()
                        ) {

                            relatedTitle.visibility =
                                View.GONE

                            relatedRecyclerView.visibility =
                                View.GONE

                        } else {

                            relatedTitle.visibility =
                                View.VISIBLE

                            relatedRecyclerView.visibility =
                                View.VISIBLE

                            relatedRecyclerView.adapter =
                                ProductAdapter(
                                    relatedProducts
                                )
                        }
                    }

                    override fun onCancelled(
                        error: DatabaseError
                    ) {

                        Toast.makeText(
                            this@ProductDetailsActivity,
                            "Related products error: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
    }
}