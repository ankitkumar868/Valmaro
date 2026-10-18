package com.example.valmaro

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

data class WatchlistItem(
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val discountPrice: Double = 0.0,
    val discountPercent: Int = 0,
    val imageUrl: String = "",
    val category: String = ""
)

class WatchlistActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var countText: TextView
    private lateinit var emptyText: TextView

    private lateinit var adapter: WatchlistAdapter

    private val watchlistItems = mutableListOf<WatchlistItem>()

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private var watchlistListener: ValueEventListener? = null
    private var watchlistRef: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watchlist)

        recyclerView = findViewById(R.id.watchlistRecyclerView)
        countText = findViewById(R.id.watchlistCountText)
        emptyText = findViewById(R.id.emptyWatchlistText)

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = WatchlistAdapter(
            items = watchlistItems,
            onRemoveClick = { item ->
                removeFromWatchlist(item.productId)
            }
        )

        recyclerView.adapter = adapter

        findViewById<View>(R.id.backButton).setOnClickListener {
            finish()
        }

        loadWatchlist()
    }

    private fun getDoubleValue(snapshot: DataSnapshot): Double {
        val value = snapshot.value ?: return 0.0

        return value.toString()
            .replace("₹", "")
            .replace(",", "")
            .trim()
            .toDoubleOrNull() ?: 0.0
    }

    private fun loadWatchlist() {

        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(
                this,
                "Please login first",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        watchlistRef = database
            .getReference("users")
            .child(user.uid)
            .child("watchlist")

        watchlistListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                watchlistItems.clear()

                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    updateUI()
                    return
                }

                val productIds = snapshot.children
                    .filter { it.getValue(Boolean::class.java) == true }
                    .map { it.key ?: "" }
                    .filter { it.isNotBlank() }

                if (productIds.isEmpty()) {
                    updateUI()
                    return
                }

                var completed = 0

                productIds.forEach { productId ->

                    database.getReference("products")
                        .child(productId)
                        .get()
                        .addOnSuccessListener { productSnapshot ->

                            if (productSnapshot.exists() &&
                                productSnapshot.value is Map<*, *>
                            ) {

                                val item = WatchlistItem(
                                    productId = productId,

                                    name = productSnapshot.child("name")
                                        .getValue(String::class.java) ?: "",

                                    price = getDoubleValue(
                                        productSnapshot.child("price")
                                    ),

                                    discountPrice = getDoubleValue(
                                        productSnapshot.child("discountPrice")
                                    ),

                                    discountPercent = (
                                            productSnapshot.child("discountPercent").value
                                                ?.toString()
                                                ?.toDoubleOrNull()
                                                ?: productSnapshot.child("discountPercentage").value
                                                    ?.toString()
                                                    ?.toDoubleOrNull()
                                                ?: 0.0
                                            ).toInt(),

                                    imageUrl = productSnapshot.child("imageUrl")
                                        .getValue(String::class.java) ?: "",

                                    category = productSnapshot.child("category")
                                        .getValue(String::class.java) ?: ""
                                )

                                watchlistItems.add(item)
                            }

                            completed++

                            if (completed == productIds.size) {
                                updateUI()
                            }
                        }
                        .addOnFailureListener {

                            completed++

                            if (completed == productIds.size) {
                                updateUI()
                            }
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {

                Toast.makeText(
                    this@WatchlistActivity,
                    "Watchlist error: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        watchlistRef?.addValueEventListener(watchlistListener!!)
    }

    private fun updateUI() {

        adapter.notifyDataSetChanged()

        countText.text = "${watchlistItems.size} Products"

        if (watchlistItems.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun removeFromWatchlist(productId: String) {

        val user = auth.currentUser ?: return

        database.getReference("users")
            .child(user.uid)
            .child("watchlist")
            .child(productId)
            .removeValue()
            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Removed from Watchlist",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    this,
                    "Remove failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (watchlistListener != null) {
            watchlistRef?.removeEventListener(watchlistListener!!)
        }
    }
}


// ===============================
// WATCHLIST ADAPTER
// ===============================

class WatchlistAdapter(
    private val items: List<WatchlistItem>,
    private val onRemoveClick: (WatchlistItem) -> Unit
) : RecyclerView.Adapter<WatchlistAdapter.WatchlistViewHolder>() {

    class WatchlistViewHolder(
        val layout: LinearLayout,
        val image: ImageView,
        val name: TextView,
        val category: TextView,
        val price: TextView,
        val discountPrice: TextView,
        val discountBadge: TextView,
        val removeButton: Button
    ) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WatchlistViewHolder {

        val context = parent.context

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(12, 12, 12, 12)
            setBackgroundColor(android.graphics.Color.WHITE)

            val params = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            params.setMargins(0, 0, 0, 12)
            layoutParams = params
        }

        val image = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(105, 115)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        val details = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 0, 0, 0)

            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val name = TextView(context).apply {
            textSize = 16f
            setTextColor(android.graphics.Color.BLACK)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val category = TextView(context).apply {
            textSize = 12f
            setTextColor(android.graphics.Color.GRAY)
        }

        val price = TextView(context).apply {
            textSize = 14f
            setTextColor(android.graphics.Color.GRAY)
        }

        val discountPrice = TextView(context).apply {
            textSize = 18f
            setTextColor(android.graphics.Color.RED)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val discountBadge = TextView(context).apply {
            textSize = 12f
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(android.graphics.Color.RED)
            setPadding(8, 4, 8, 4)
        }

        val removeButton = Button(context).apply {
            text = "♥ Remove"
            textSize = 12f
            setTextColor(android.graphics.Color.RED)
            isAllCaps = false
        }

        details.addView(name)
        details.addView(category)
        details.addView(price)
        details.addView(discountPrice)
        details.addView(discountBadge)
        details.addView(removeButton)

        container.addView(image)
        container.addView(details)

        return WatchlistViewHolder(
            container,
            image,
            name,
            category,
            price,
            discountPrice,
            discountBadge,
            removeButton
        )
    }

    override fun
            onBindViewHolder(
        holder: WatchlistViewHolder,
        position: Int
    ) {

        val item = items[position]

        holder.name.text = item.name
        holder.category.text = item.category

        Glide.with(holder.image.context)
            .load(item.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .centerCrop()
            .into(holder.image)

        if (item.discountPrice > 0 &&
            item.discountPrice < item.price
        ) {

            holder.price.text = "₹${item.price}"
            holder.price.paintFlags =
                holder.price.paintFlags or
                        android.graphics.Paint.STRIKE_THRU_TEXT_FLAG

            holder.discountPrice.visibility = View.VISIBLE
            holder.discountPrice.text = "₹${item.discountPrice}"

            holder.discountBadge.visibility = View.VISIBLE
            holder.discountBadge.text = "${item.discountPercent}% OFF"

        } else {

            holder.price.text = "₹${item.price}"
            holder.price.paintFlags =
                holder.price.paintFlags and
                        android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()

            holder.discountPrice.visibility = View.GONE
            holder.discountBadge.visibility = View.GONE
        }

        holder.removeButton.setOnClickListener {
            onRemoveClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}