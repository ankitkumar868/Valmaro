package com.example.valmaro.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.valmaro.R
import com.example.valmaro.model.WatchlistItem

class WatchlistAdapter(
    private val items: MutableList<WatchlistItem>,
    private val onRemoveClick: (WatchlistItem) -> Unit,
    private val onItemClick: (WatchlistItem) -> Unit
) : RecyclerView.Adapter<WatchlistAdapter.WatchlistViewHolder>() {

    class WatchlistViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val image: ImageView =
            itemView.findViewById(R.id.watchlistProductImage)

        val name: TextView =
            itemView.findViewById(R.id.watchlistProductName)

        val category: TextView =
            itemView.findViewById(R.id.watchlistProductCategory)

        val rating: TextView =
            itemView.findViewById(R.id.watchlistProductRating)

        val originalPrice: TextView =
            itemView.findViewById(R.id.watchlistOriginalPrice)

        val discountPrice: TextView =
            itemView.findViewById(R.id.watchlistDiscountPrice)

        val discountBadge: TextView =
            itemView.findViewById(R.id.watchlistDiscountBadge)

        val removeButton: Button =
            itemView.findViewById(R.id.removeWatchlistButton)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WatchlistViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_watchlist, parent, false)

        return WatchlistViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: WatchlistViewHolder,
        position: Int
    ) {

        val item = items[position]

        // PRODUCT IMAGE
        if (item.imageUrl.isNotBlank()) {
            Glide.with(holder.itemView.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.image)
        } else {
            holder.image.setImageResource(
                R.drawable.ic_launcher_background
            )
        }

        // PRODUCT DETAILS
        holder.name.text = item.name
        holder.category.text = item.category
        holder.rating.text = "⭐ ${item.rating}"

        // PRICE
        val original = item.price
            .replace("₹", "")
            .replace(",", "")
            .trim()
            .toDoubleOrNull() ?: 0.0

        val discounted = item.discountPrice
            .replace("₹", "")
            .replace(",", "")
            .trim()
            .toDoubleOrNull() ?: 0.0

        val hasDiscount =
            discounted > 0 &&
                    original > 0 &&
                    discounted < original &&
                    item.discountPercent > 0

        if (hasDiscount) {

            holder.originalPrice.visibility = View.VISIBLE
            holder.originalPrice.text = item.price

            holder.originalPrice.paintFlags =
                holder.originalPrice.paintFlags or
                        Paint.STRIKE_THRU_TEXT_FLAG

            holder.discountPrice.visibility = View.VISIBLE
            holder.discountPrice.text = item.discountPrice

            holder.discountBadge.visibility = View.VISIBLE
            holder.discountBadge.text =
                "${item.discountPercent}% OFF"

        } else {

            holder.originalPrice.visibility = View.GONE

            holder.discountPrice.visibility = View.VISIBLE
            holder.discountPrice.text = item.price

            holder.discountBadge.visibility = View.GONE
        }

        // REMOVE FROM WATCHLIST
        holder.removeButton.setOnClickListener {
            onRemoveClick(item)
        }

        // OPEN PRODUCT DETAILS
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    // UPDATE WATCHLIST
    fun updateItems(newItems: List<WatchlistItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    // REMOVE ITEM FROM LIST
    fun removeItem(productId: String) {
        val index = items.indexOfFirst {
            it.productId == productId
        }

        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}