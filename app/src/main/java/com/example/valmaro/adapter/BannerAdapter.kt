package com.example.valmaro.adapter

import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.valmaro.R

class BannerAdapter(
    private val banners: List<Int>
): RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {
    class BannerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.bannerImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)

        return BannerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.image.setImageResource(banners[position])

    }

    override fun getItemCount(): Int {
        return banners.size
    }
}