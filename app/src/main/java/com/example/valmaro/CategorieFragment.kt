package com.example.valmaro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.valmaro.databinding.FragmentCategorieBinding
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

class CategorieFragment : Fragment() {

    private var _binding: FragmentCategorieBinding? = null
    private val binding get() = _binding!!

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter

    private var productContext = ""
    private var orderContext = ""
    private var ordersLoaded = false
    private val availableProducts = mutableListOf<com.example.valmaro.model.Product>()
    private var isLoading = false

    private val model by lazy {
        Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel("gemini-3.8-flash")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCategorieBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupChat()
        loadProducts()
        loadUserOrders()
    }

    // Chat setup
    private fun setupChat() {

        chatAdapter = ChatAdapter(messages)

        binding.chatRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        binding.chatRecyclerView.adapter = chatAdapter

        addMessage(
            "Namaste! 👋 Main Valmaro AI Assistant hoon. " +
                    "Aapko kis product ki zarurat hai?",
            false
        )

        binding.sendButton.setOnClickListener {

            val userMessage =
                binding.messageInput.text.toString().trim()

            if (userMessage.isEmpty()) {
                return@setOnClickListener
            }

            if (isLoading) {
                return@setOnClickListener
            }

            if (productContext.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Products are still loading...",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }
            if (!ordersLoaded) {
                Toast.makeText(
                    requireContext(),
                    "Orders are still loading...",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            binding.messageInput.text?.clear()

            // User ka message add karo
            addMessage(userMessage, true)

            // Gemini ko message bhejo
            sendToGemini(userMessage)
        }
    }

    // Firebase se products load karna
    private fun loadProducts() {

        FirebaseDatabase.getInstance()
            .getReference("products")
            .addListenerForSingleValueEvent(

                object : ValueEventListener {

                    override fun onDataChange(
                        snapshot: DataSnapshot
                    ) {

                        val productList = mutableListOf<String>()
                        availableProducts.clear()

                        for (item in snapshot.children) {

                            val name =
                                item.child("name").value?.toString() ?: ""

                            val category =
                                item.child("category").value?.toString() ?: ""

                            val price =
                                item.child("price").value?.toString() ?: ""

                            val rating =
                                item.child("rating").value?.toString() ?: ""

                            val description =
                                item.child("description").value?.toString() ?: ""



                            val stock =
                                item.child("stock").value?.toString()
                                    ?.toIntOrNull() ?: 0

                            if (stock > 0 && name.isNotBlank()) {
                                val product = com.example.valmaro.model.Product(
                                    name = name,
                                    price = price,
                                    rating = rating,
                                    image = item.child("image").value?.toString()?.toIntOrNull() ?: 0,
                                    description = description,
                                    category = category,
                                    stock = stock,
                                    productId = item.key ?: ""
                                )

                                availableProducts.add(product)

                                productList.add(
                                    """
                                    Product ID: ${item.key}
                                    Name: $name
                                    Category: $category
                                    Price: $price
                                    Rating: $rating
                                    Description: $description
                                    Stock: $stock
                                    """.trimIndent()
                                )
                            }
                        }

                        productContext = productList.joinToString("\n\n")
                    }

                    override fun onCancelled(
                        error: DatabaseError
                    ) {

                        if (!isAdded) return

                        Toast.makeText(
                            requireContext(),
                            "Firebase Error: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
    }

    // Logged-in user ke orders load karna
    private fun loadUserOrders() {

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            orderContext = ""
            ordersLoaded = true
            return
        }

        val uid = user.uid

        FirebaseDatabase.getInstance()
            .getReference("orders")
            .child(uid)
            .get()
            .addOnSuccessListener { snapshot ->

                val orderList = mutableListOf<String>()

                for (orderSnapshot in snapshot.children) {

                    val orderId =
                        orderSnapshot.child("orderId")
                            .getValue(String::class.java) ?: ""

                    val productName =
                        orderSnapshot.child("productName")
                            .getValue(String::class.java) ?: ""

                    val price =
                        orderSnapshot.child("price")
                            .getValue(String::class.java) ?: ""

                    val quantity =
                        orderSnapshot.child("quantity")
                            .getValue(Int::class.java) ?: 0

                    val status =
                        orderSnapshot.child("status")
                            .getValue(String::class.java) ?: "Unknown"

                    val orderDate =
                        orderSnapshot.child("orderDate")
                            .getValue(Long::class.java) ?: 0L

                    orderList.add(
                        """
                    Order ID: $orderId
                    Product: $productName
                    Price: $price
                    Quantity: $quantity
                    Status: $status
                    Order Date: $orderDate
                    """.trimIndent()
                    )
                }

                orderContext = if (orderList.isEmpty()) {
                    "No orders found for this user."
                } else {
                    orderList.joinToString("\n\n")
                }

                ordersLoaded = true
            }
            .addOnFailureListener {

                orderContext = ""
                ordersLoaded = true
            }
    }

    // Gemini ko message bhejna
    private fun sendToGemini(userMessage: String) {

        isLoading = true
        binding.sendButton.isEnabled = false

        // Gemini reply generate karte waqt Typing bubble show hoga
        addMessage("Typing...", false)

        viewLifecycleOwner.lifecycleScope.launch {

            try {

                // Typing bubble ko history mein include nahi karna
                val conversationHistory = messages
                    .filter { it.message != "Typing..." }
                    .dropLast(1)
                    .takeLast(10)
                    .joinToString("\n") {

                        val role = if (it.isUser) "User" else "Assistant"

                        "$role: ${it.message}"
                    }

                val prompt = """
                    You are Valmaro AI Shopping Assistant.

                    Help users find suitable products from the
                    available product catalog.

                    AVAILABLE PRODUCTS:
                    $productContext
                    
                    YOUR ORDERS:
                    $orderContext

                    CONVERSATION HISTORY:
                    $conversationHistory

                    CURRENT USER MESSAGE:
                    $userMessage

                    RULES:
                    1. Answer in the user's language.
                    2. Recommend only products from the catalog.
                    3. Never invent product names, prices, or ratings.
                    4. Respect the user's budget.
                    5. Recommend at most 3 suitable products.
                    6. If no suitable product exists, say so honestly.
                    7. Keep responses short and helpful.
                    8. Do not claim that you placed an order.
                """.trimIndent()

                // Gemini ka reply generate hoga
                val response = model.generateContent(prompt)

                val answer = response.text
                    ?: "Sorry, mujhe abhi jawab nahi mil paya."

                // Typing bubble remove karo
                removeTypingMessage()

// Gemini ka reply show karo
                addMessage(answer, false)

// Recommended products ke cards
                val matchedProducts = availableProducts.filter { product ->
                    answer.contains(product.name, ignoreCase = true)
                }

                matchedProducts.distinctBy { it.productId }
                    .take(3)
                    .forEach { product ->
                        addProductMessage(product)
                    }

                // Reply aane ke baad Typing bubble remove karo
                removeTypingMessage()

                // Gemini ka actual reply show karo
                addMessage(answer, false)

            } catch (e: Exception) {

                // Error aane par bhi Typing bubble remove karo
                // Reply aane ke baad Typing bubble remove karo
                removeTypingMessage()

                addMessage(
                    "Sorry, AI se connect nahi ho paya. Dobara try karo.",
                    false
                )

                android.util.Log.e(
                    "ValmaroChat",
                    "Gemini error",
                    e
                )

            } finally {

                isLoading = false

                _binding?.sendButton?.isEnabled = true
            }
        }
    }

    // Sirf Typing bubble ko remove karna
    private fun removeTypingMessage() {

        val typingIndex = messages.indexOfLast {
            it.message == "Typing..." && !it.isUser
        }

        if (typingIndex != -1) {

            messages.removeAt(typingIndex)

            chatAdapter.notifyItemRemoved(typingIndex)
        }
    }

    // Message list me add karna
    private fun addMessage(
        message: String,
        isUser: Boolean
    ) {

        messages.add(
            ChatMessage(
                message = message,
                isUser = isUser
            )
        )

        val position = messages.lastIndex

        chatAdapter.notifyItemInserted(position)

        binding.chatRecyclerView.scrollToPosition(position)
    }
    private fun addProductMessage(
        product: com.example.valmaro.model.Product
    ) {

        messages.add(
            ChatMessage(
                message = "",
                isUser = false,
                product = product
            )
        )

        val position = messages.lastIndex

        chatAdapter.notifyItemInserted(position)

        binding.chatRecyclerView.scrollToPosition(position)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}