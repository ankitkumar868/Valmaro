package com.example.valmaro.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.valmaro.R

data class Category(
    val name: String,
    val image: Int
)

class CategoryAdapter(
    private val categories: List<Category>,
    private val onCategoryClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val image: ImageView =
            itemView.findViewById(R.id.categoryImage)

        val name: TextView =
            itemView.findViewById(R.id.categoryName)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)

        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CategoryViewHolder,
        position: Int
    ) {

        val category = categories[position]

        holder.name.text = category.name
        holder.image.setImageResource(category.image)

        // Category click
        holder.itemView.setOnClickListener {
            onCategoryClick(category.name)
        }
    }

    override fun getItemCount(): Int {
        return categories.size
    }
}