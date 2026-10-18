package com.example.valmaro

import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(
    private val messages: MutableList<ChatMessage>
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val container: LinearLayout =
            itemView.findViewById(R.id.chatBubbleContainer)

        val messageText: TextView =
            itemView.findViewById(R.id.chatMessageText)

        val viewDetailsButton: Button =
            itemView.findViewById(R.id.viewDetailsButton)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)

        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ChatViewHolder,
        position: Int
    ) {

        val message = messages[position]
        val product = message.product

        val params = holder.container.layoutParams
                as FrameLayout.LayoutParams

        if (message.isUser) {

            params.gravity = Gravity.END
            holder.container.setBackgroundResource(
                R.drawable.bg_chat_user
            )

        } else {

            params.gravity = Gravity.START
            holder.container.setBackgroundResource(
                R.drawable.bg_chat_ai
            )
        }

        holder.container.layoutParams = params

        if (product != null) {

            holder.messageText.text =
                "🛍️ ${product.name}\n\n" +
                        "💰 Price: ${product.price}\n" +
                        "⭐ Rating: ${product.rating}\n\n" +
                        product.description

            holder.viewDetailsButton.visibility = View.VISIBLE

            holder.viewDetailsButton.setOnClickListener {

                val intent = Intent(
                    holder.itemView.context,
                    ProductDetailsActivity::class.java
                )

                intent.putExtra("product_name", product.name)
                intent.putExtra("product_price", product.price)
                intent.putExtra("product_rating", product.rating)
                intent.putExtra("product_description", product.description)
                intent.putExtra("product_category", product.category)
                intent.putExtra("product_stock", product.stock)
                intent.putExtra("product_image", product.image)
                intent.putExtra("product_id", product.productId)

                holder.itemView.context.startActivity(intent)
            }

        } else {

            holder.messageText.text = message.message
            holder.viewDetailsButton.visibility = View.GONE
            holder.viewDetailsButton.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}