package com.example.valmaro

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.valmaro.utils.PriceHistoryManager
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class SellerActivity : AppCompatActivity() {

    private lateinit var productImagePreview: ImageView
    private lateinit var productNameInput: EditText
    private lateinit var productPriceInput: EditText
    private lateinit var productDiscountPriceInput: EditText
    private lateinit var productDiscountPercentPreview: TextView
    private lateinit var productStockInput: EditText
    private lateinit var productDescriptionInput: EditText
    private lateinit var productCategorySpinner: Spinner
    private lateinit var uploadProductButton: Button

    private var selectedImageUri: Uri? = null

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                productImagePreview.setImageURI(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller)

        productImagePreview = findViewById(R.id.productImagePreview)
        productNameInput = findViewById(R.id.productNameInput)
        productPriceInput = findViewById(R.id.productPriceInput)

        // Discount fields
        productDiscountPriceInput =
            findViewById(R.id.productDiscountPriceInput)

        productDiscountPercentPreview =
            findViewById(R.id.productDiscountPercentPreview)

        productStockInput = findViewById(R.id.productStockInput)
        productDescriptionInput = findViewById(R.id.productDescriptionInput)
        productCategorySpinner = findViewById(R.id.productCategorySpinner)
        uploadProductButton = findViewById(R.id.uploadProductButton)

        val categories = arrayOf(
            "Select Category",
            "Shoes",
            "Clothes",
            "Watches"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        productCategorySpinner.adapter = adapter

        // Calculate discount percentage automatically
        val priceWatcher = object : android.text.TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                calculateDiscount()
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        }

        productPriceInput.addTextChangedListener(priceWatcher)
        productDiscountPriceInput.addTextChangedListener(priceWatcher)

        findViewById<Button>(R.id.selectImageButton).setOnClickListener {
            imagePicker.launch("image/*")
        }

        uploadProductButton.setOnClickListener {
            uploadProduct()
        }
    }

    private fun calculateDiscount() {

        val originalPrice =
            productPriceInput.text.toString().toDoubleOrNull()

        val discountPrice =
            productDiscountPriceInput.text.toString().toDoubleOrNull()

        if (originalPrice != null &&
            discountPrice != null &&
            originalPrice > 0 &&
            discountPrice >= 0 &&
            discountPrice < originalPrice
        ) {

            val discountPercent =
                (((originalPrice - discountPrice) / originalPrice) * 100)
                    .toInt()

            productDiscountPercentPreview.text =
                "Discount: $discountPercent%"

        } else {

            productDiscountPercentPreview.text = "Discount: 0%"
        }
    }

    private fun uploadImageToStorage(
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {

        val storageRef = FirebaseStorage.getInstance()
            .reference
            .child("product_images/${System.currentTimeMillis()}.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {

                storageRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        onSuccess(uri.toString())
                    }
                    .addOnFailureListener { error ->
                        onFailure(error.message ?: "Image URL failed")
                    }
            }
            .addOnFailureListener { error ->
                onFailure(error.message ?: "Image upload failed")
            }
    }

    private fun uploadProduct() {

        val name = productNameInput.text.toString().trim()
        val price = productPriceInput.text.toString().trim()
        val discountPriceText =
            productDiscountPriceInput.text.toString().trim()

        val stock = productStockInput.text.toString().trim()
        val description = productDescriptionInput.text.toString().trim()
        val category = productCategorySpinner.selectedItem.toString()

        if (name.isEmpty() ||
            price.isEmpty() ||
            discountPriceText.isEmpty() ||
            stock.isEmpty() ||
            description.isEmpty() ||
            category == "Select Category"
        ) {

            Toast.makeText(
                this,
                "Please fill all details",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val productPrice = price.toDoubleOrNull()
        val discountPrice = discountPriceText.toDoubleOrNull()
        val productStock = stock.toIntOrNull()

        if (productPrice == null || productPrice <= 0) {
            productPriceInput.error = "Enter a valid price"
            return
        }

        if (discountPrice == null ||
            discountPrice < 0 ||
            discountPrice >= productPrice
        ) {

            productDiscountPriceInput.error =
                "Discount price must be less than original price"

            return
        }

        if (productStock == null || productStock < 0) {
            productStockInput.error = "Enter valid stock"
            return
        }

        val discountPercent =
            (((productPrice - discountPrice) / productPrice) * 100)
                .toInt()

        val product = HashMap<String, Any>()

        product["name"] = name
        product["price"] = productPrice
        product["discountPrice"] = discountPrice
        product["discountPercent"] = discountPercent
        product["category"] = category
        product["stock"] = productStock
        product["description"] = description
        product["rating"] = 0.0

        val database = FirebaseDatabase.getInstance()
        val productRef = database.getReference("products").push()

        val imageUri = selectedImageUri

        if (imageUri == null) {
            Toast.makeText(
                this,
                "Please select a product image",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        uploadProductButton.isEnabled = false

        uploadImageToStorage(
            imageUri = imageUri,

            onSuccess = { imageUrl ->

                product["imageUrl"] = imageUrl

                productRef.setValue(product)
                    .addOnSuccessListener {

                        PriceHistoryManager.savePrice(
                            productId = productRef.key ?: "",
                            price = productPrice
                        )

                        Toast.makeText(
                            this,
                            "Product uploaded successfully!",
                            Toast.LENGTH_LONG
                        ).show()

                        // Clear form after successful upload
                        productNameInput.text.clear()
                        productPriceInput.text.clear()
                        productDiscountPriceInput.text.clear()
                        productStockInput.text.clear()
                        productDescriptionInput.text.clear()
                        productCategorySpinner.setSelection(0)
                        productImagePreview.setImageResource(
                            android.R.drawable.ic_menu_gallery
                        )
                        selectedImageUri = null
                        productDiscountPercentPreview.text = "Discount: 0%"

                        uploadProductButton.isEnabled = true
                    }
                    .addOnFailureListener { error ->

                        uploadProductButton.isEnabled = true

                        Toast.makeText(
                            this,
                            "Upload failed: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            },

            onFailure = { error ->

                uploadProductButton.isEnabled = true

                Toast.makeText(
                    this,
                    error,
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }
}