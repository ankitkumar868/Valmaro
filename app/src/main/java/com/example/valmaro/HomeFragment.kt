package com.example.valmaro

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.valmaro.adapter.BannerAdapter
import com.example.valmaro.adapter.Category
import com.example.valmaro.adapter.CategoryAdapter
import com.example.valmaro.adapter.ProductAdapter
import com.example.valmaro.databinding.FragmentHomeBinding
import com.example.valmaro.model.FirebaseProduct
import com.example.valmaro.model.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import org.json.JSONObject

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val allProducts = mutableListOf<Product>()

    private lateinit var bannerAdapter: BannerAdapter

    private val handler = Handler(Looper.getMainLooper())

    private var productListener: ValueEventListener? = null

    private val bannerRunnable = object : Runnable {
        override fun run() {
            if (_binding != null &&
                ::bannerAdapter.isInitialized &&
                bannerAdapter.itemCount > 0
            ) {
                val currentPosition = binding.bannerSlider.currentItem
                val nextPosition =
                    (currentPosition + 1) % bannerAdapter.itemCount

                binding.bannerSlider.currentItem = nextPosition
                handler.postDelayed(this, 3000)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(
            inflater,
            container,
            false
        )

        setupBanner()
        setupCategories()
        setupSearch()
        setupAiRecommendation()

        FirebaseTest.testConnection()

        return binding.root
    }

    // =========================
    // BANNER
    // =========================

    private fun setupBanner() {

        val banners = listOf(
            R.drawable.img_4,
            R.drawable.img_2,
            R.drawable.img_3
        )

        bannerAdapter = BannerAdapter(banners)
        binding.bannerSlider.adapter = bannerAdapter

        handler.removeCallbacks(bannerRunnable)
        handler.postDelayed(bannerRunnable, 3000)
    }

    // =========================
    // CATEGORIES
    // =========================

    private fun setupCategories() {

        val categories = listOf(
            Category("Shoes", R.drawable.img),
            Category("Clothes", R.drawable.img_1),
            Category("Watches", R.drawable.image2)
        )

        val adapter = CategoryAdapter(categories) { selectedCategory ->
            filterProducts(selectedCategory)
        }

        binding.categoryRecyclerView.layoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )

        binding.categoryRecyclerView.adapter = adapter
    }

    // =========================
    // LOAD PRODUCTS FROM FIREBASE
    // =========================

    private fun setupProducts() {

        val productRef =
            FirebaseDatabase.getInstance().getReference("products")

        productListener?.let {
            productRef.removeEventListener(it)
        }

        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (_binding == null) return

                allProducts.clear()

                for (productSnapshot in snapshot.children) {

                    // Skip entries that are not product objects.
                    val rawValue = productSnapshot.value

                    if (rawValue !is Map<*, *>) {
                        Log.w(
                            "VALMARO_PRODUCTS",
                            "Skipping non-product entry: ${productSnapshot.key}"
                        )
                        continue
                    }

                    try {

                        val firebaseProduct =
                            productSnapshot.getValue(
                                FirebaseProduct::class.java
                            ) ?: continue

                        // =========================
                        // IMAGE BY CATEGORY
                        // =========================

                        val productImage = when {
                            firebaseProduct.category.trim()
                                .equals("Shoes", ignoreCase = true) ->
                                R.drawable.shoes

                            firebaseProduct.category.trim()
                                .equals("Clothes", ignoreCase = true) ->
                                R.drawable.tshirt

                            firebaseProduct.category.trim()
                                .equals("Watches", ignoreCase = true) ->
                                R.drawable.watch

                            else -> R.drawable.shoes
                        }

                        // =========================
                        // PRICE AND DISCOUNT
                        // =========================

                        val originalPriceValue =
                            firebaseProduct.price
                                .toString()
                                .replace("₹", "")
                                .replace(",", "")
                                .trim()
                                .toDoubleOrNull() ?: 0.0

                        val originalPriceText =
                            "₹${originalPriceValue.toInt()}"

                        val discountPriceValue =
                            firebaseProduct.discountPrice
                                ?.toString()
                                ?.replace("₹", "")
                                ?.replace(",", "")
                                ?.trim()
                                ?.toDoubleOrNull() ?: 0.0

                        val discountPriceText =
                            if (discountPriceValue > 0 &&
                                discountPriceValue < originalPriceValue
                            ) {
                                "₹${discountPriceValue.toInt()}"
                            } else {
                                ""
                            }

                        val discountPercent =
                            firebaseProduct.discountPercent

                        // =========================
                        // CREATE PRODUCT
                        // =========================

                        val product = Product(
                            name = firebaseProduct.name,
                            price = originalPriceText,
                            rating = firebaseProduct.rating.toString(),
                            image = productImage,
                            description = firebaseProduct.description,
                            category = firebaseProduct.category,
                            stock = firebaseProduct.stock,
                            sales = firebaseProduct.sales,
                            views = firebaseProduct.views,
                            productId = productSnapshot.key ?: "",
                            imageUrl = firebaseProduct.imageUrl,
                            discountPrice = discountPriceText,
                            discountPercent = firebaseProduct.discountPercent
                        )

                        allProducts.add(product)

                    } catch (e: Exception) {

                        Log.e(
                            "VALMARO_PRODUCTS",
                            "Failed to parse product: ${productSnapshot.key}",
                            e
                        )
                    }
                }

                Log.d(
                    "VALMARO_PRODUCTS",
                    "Total products loaded: ${allProducts.size}"
                )

                showProducts(allProducts)
            }

            override fun onCancelled(error: DatabaseError) {

                if (_binding == null) return

                Toast.makeText(
                    requireContext(),
                    "Firebase Error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        productListener = listener
        productRef.addListenerForSingleValueEvent(listener)
    }

    // =========================
    // FILTER CATEGORY
    // =========================

    private fun filterProducts(category: String) {

        if (allProducts.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Products are still loading...",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val filteredProducts = allProducts.filter {
            it.category.trim().equals(
                category.trim(),
                ignoreCase = true
            )
        }

        if (filteredProducts.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "No products found in $category",
                Toast.LENGTH_SHORT
            ).show()
        }

        showProducts(filteredProducts)
    }

    // =========================
    // AI RECOMMENDATION
    // =========================

    private fun setupAiRecommendation() {

        binding.findProductsButton.setOnClickListener {

            val userNeed = binding.aiNeedInput.text
                .toString()
                .trim()

            if (userNeed.isEmpty()) {
                binding.aiNeedInput.error = "Tell me what you need"
                return@setOnClickListener
            }

            if (allProducts.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Products are still loading...",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            binding.findProductsButton.isEnabled = false
            binding.aiResultTitle.visibility = View.VISIBLE
            binding.aiResultTitle.text = "🤖 Finding products..."

            viewLifecycleOwner.lifecycleScope.launch {

                try {

                    val response =
                        GeminiRecommendation.getRecommendations(
                            userNeed,
                            allProducts
                        )

                    val cleanResponse = response
                        .replace("```json", "")
                        .replace("```", "")
                        .trim()

                    val jsonObject = JSONObject(cleanResponse)
                    val productIds = jsonObject.getJSONArray("productIds")

                    val recommendedProducts = mutableListOf<Product>()

                    for (i in 0 until productIds.length()) {

                        val id = productIds.getString(i)

                        val product = allProducts.find {
                            it.productId == id && it.stock > 0
                        }

                        if (product != null &&
                            !recommendedProducts.any {
                                it.productId == product.productId
                            }
                        ) {
                            recommendedProducts.add(product)
                        }

                        if (recommendedProducts.size == 3) break
                    }

                    if (_binding == null) return@launch

                    if (recommendedProducts.isEmpty()) {

                        binding.aiResultTitle.text =
                            "🤖 No suitable products found"

                        binding.aiProductsRecyclerView.visibility =
                            View.GONE

                    } else {

                        binding.aiResultTitle.text =
                            jsonObject.optString(
                                "message",
                                "🤖 Recommended For You"
                            )

                        binding.aiProductsRecyclerView.visibility =
                            View.VISIBLE

                        binding.aiProductsRecyclerView.layoutManager =
                            GridLayoutManager(requireContext(), 2)

                        binding.aiProductsRecyclerView.adapter =
                            ProductAdapter(recommendedProducts)
                    }

                } catch (e: Exception) {

                    Log.e("GeminiAI", "Recommendation error", e)

                    if (_binding == null) return@launch

                    binding.aiResultTitle.text =
                        "🤖 AI recommendation failed. Please try again."

                    binding.aiProductsRecyclerView.visibility =
                        View.GONE

                    Toast.makeText(
                        requireContext(),
                        "Error: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()

                } finally {

                    if (_binding != null) {
                        binding.findProductsButton.isEnabled = true
                    }
                }
            }
        }
    }

    // =========================
    // SEARCH
    // =========================

    private fun setupSearch() {

        binding.searchView.setOnQueryTextListener(
            object : android.widget.SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    searchProducts(query)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    searchProducts(newText)
                    return true
                }
            }
        )
    }

    private fun searchProducts(query: String?) {

        val searchText = query
            ?.trim()
            ?.lowercase()
            ?: ""

        if (searchText.isEmpty()) {
            showProducts(allProducts)
            return
        }

        val filteredProducts = allProducts.filter {
            it.name.lowercase().contains(searchText) ||
                    it.category.lowercase().contains(searchText) ||
                    it.description.lowercase().contains(searchText)
        }

        showProducts(filteredProducts)
    }

    // =========================
    // SHOW PRODUCTS
    // =========================

    private fun showProducts(products: List<Product>) {

        if (_binding == null) return

        Log.d(
            "VALMARO_PRODUCTS",
            "Showing products: ${products.size}"
        )

        binding.productRecyclerView.layoutManager =
            GridLayoutManager(requireContext(), 2)

        binding.productRecyclerView.isNestedScrollingEnabled = false

        binding.productRecyclerView.adapter =
            ProductAdapter(products)
    }

    // =========================
    // LIFECYCLE
    // =========================

    override fun onResume() {
        super.onResume()
        setupProducts()
    }

    override fun onDestroyView() {

        handler.removeCallbacks(bannerRunnable)

        productListener?.let {
            FirebaseDatabase.getInstance()
                .getReference("products")
                .removeEventListener(it)
        }

        productListener = null

        super.onDestroyView()
        _binding = null
    }
}