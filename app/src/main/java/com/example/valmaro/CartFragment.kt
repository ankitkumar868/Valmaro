package com.example.valmaro

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.valmaro.adapter.CartAdapter
import com.google.firebase.database.FirebaseDatabase

class CartFragment : Fragment() {

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var cartTotal: TextView
    private lateinit var checkoutButton: Button
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_cart,
            container,
            false
        )

        cartRecyclerView =
            view.findViewById(R.id.cartRecyclerView)

        cartTotal =
            view.findViewById(R.id.cartTotal)

        checkoutButton =
            view.findViewById(R.id.checkoutButton)

        cartRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        setupCart()

        checkoutButton.setOnClickListener {

            if (CartManager.cartItems.isEmpty()) {

                Toast.makeText(
                    requireContext(),
                    "Your cart is empty",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            checkCartStockBeforeCheckout()
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        if (::cartAdapter.isInitialized) {
            cartAdapter.notifyDataSetChanged()
        }

        if (::cartTotal.isInitialized) {
            updateTotal()
        }
    }

    private fun setupCart() {

        FirebaseDatabase.getInstance()
            .getReference("products")
            .get()
            .addOnSuccessListener { snapshot ->

                // --------------------------------
                // STOCK MAP
                // --------------------------------
                val stockMap =
                    mutableMapOf<String, Int>()

                for (productSnapshot in snapshot.children) {

                    val firebaseKey =
                        productSnapshot.key ?: ""

                    val productId =
                        productSnapshot
                            .child("productId")
                            .getValue(String::class.java)

                    val name =
                        productSnapshot
                            .child("name")
                            .getValue(String::class.java)

                    val stock =
                        productSnapshot
                            .child("stock")
                            .getValue(Int::class.java)
                            ?: 0

                    // Firebase key se save
                    if (firebaseKey.isNotBlank()) {
                        stockMap[firebaseKey] = stock
                    }

                    // productId se bhi save
                    if (!productId.isNullOrBlank()) {
                        stockMap[productId] = stock
                    }

                    // Name se bhi save
                    if (!name.isNullOrBlank()) {
                        stockMap[name] = stock
                    }
                }

                cartAdapter = CartAdapter(
                    CartManager.cartItems,
                    stockMap
                ) {
                    updateTotal()
                }

                cartRecyclerView.adapter =
                    cartAdapter

                updateTotal()
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    requireContext(),
                    "Stock load failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()

                cartAdapter = CartAdapter(
                    CartManager.cartItems,
                    mutableMapOf()
                ) {
                    updateTotal()
                }

                cartRecyclerView.adapter =
                    cartAdapter

                updateTotal()
            }
    }

    private fun updateTotal() {

        cartTotal.text =
            "Total: ₹${CartManager.getTotal()}"
    }

    private fun checkCartStockBeforeCheckout() {

        FirebaseDatabase.getInstance()
            .getReference("products")
            .get()
            .addOnSuccessListener { snapshot ->

                for (cartItem in CartManager.cartItems) {

                    val product =
                        cartItem.product

                    val productId =
                        product.productId

                    val productName =
                        product.name

                    // --------------------------------
                    // FIND PRODUCT
                    // First productId
                    // Then name fallback
                    // --------------------------------
                    var firebaseProduct =
                        snapshot.children.firstOrNull {

                            val firebaseProductId =
                                it.child("productId")
                                    .getValue(String::class.java)

                            firebaseProductId == productId &&
                                    productId.isNotBlank()
                        }

                    // --------------------------------
                    // FALLBACK: PRODUCT NAME
                    // --------------------------------
                    if (firebaseProduct == null) {

                        firebaseProduct =
                            snapshot.children.firstOrNull {

                                val firebaseName =
                                    it.child("name")
                                        .getValue(String::class.java)

                                firebaseName?.equals(
                                    productName,
                                    ignoreCase = true
                                ) == true
                            }
                    }

                    // --------------------------------
                    // PRODUCT NOT FOUND
                    // --------------------------------
                    if (firebaseProduct == null) {

                        Toast.makeText(
                            requireContext(),
                            "$productName is no longer available",
                            Toast.LENGTH_LONG
                        ).show()

                        return@addOnSuccessListener
                    }

                    // --------------------------------
                    // LATEST STOCK
                    // --------------------------------
                    val latestStock =
                        firebaseProduct
                            .child("stock")
                            .getValue(Int::class.java)
                            ?: 0

                    // --------------------------------
                    // OUT OF STOCK
                    // --------------------------------
                    if (latestStock <= 0) {

                        Toast.makeText(
                            requireContext(),
                            "$productName is OUT OF STOCK",
                            Toast.LENGTH_LONG
                        ).show()

                        return@addOnSuccessListener
                    }

                    // --------------------------------
                    // QUANTITY CHECK
                    // --------------------------------
                    if (cartItem.quantity > latestStock) {

                        Toast.makeText(
                            requireContext(),
                            "$productName has only $latestStock item(s) available",
                            Toast.LENGTH_LONG
                        ).show()

                        return@addOnSuccessListener
                    }
                }

                // --------------------------------
                // ALL STOCK OK
                // --------------------------------
                val intent =
                    Intent(
                        requireContext(),
                        CheckoutActivity::class.java
                    )

                startActivity(intent)
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    requireContext(),
                    "Stock check failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}