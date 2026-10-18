
package com.example.valmaro

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private lateinit var profileName: TextView
    private lateinit var profilePhone: TextView
    private lateinit var profileEmail: TextView
    private lateinit var profileImage: ImageView

    private lateinit var menuEditProfile: Button
    private lateinit var myOrdersButton: Button
    private lateinit var adminOrdersButton: Button
    private lateinit var openSellerPanelButton: Button
    private lateinit var shippingDetailsButton: Button
    private lateinit var logoutButton: Button

    private lateinit var orderCountText: TextView
    private lateinit var profileRatingText: TextView
    private lateinit var watchList: LinearLayout
    private lateinit var watchListCount: TextView

    private lateinit var auth: FirebaseAuth

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                profileImage.setImageURI(uri)
                uploadProfileImage(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_profile,
            container,
            false
        )

        auth = FirebaseAuth.getInstance()

        // Initialize views
        profileName = view.findViewById(R.id.profileName)
        profilePhone = view.findViewById(R.id.profilePhone)
        profileEmail = view.findViewById(R.id.profileEmail)
        profileImage = view.findViewById(R.id.profileImage)

        menuEditProfile = view.findViewById(R.id.menuEditProfile)
        myOrdersButton = view.findViewById(R.id.myOrdersButton)
        adminOrdersButton = view.findViewById(R.id.adminOrdersButton)
        openSellerPanelButton = view.findViewById(R.id.openSellerPanelButton)
        shippingDetailsButton = view.findViewById(R.id.shippingDetailsButton)
        logoutButton = view.findViewById(R.id.logoutButton)

        orderCountText = view.findViewById(R.id.orderCountText)
        watchList = view.findViewById(R.id.watchList)
        watchListCount = view.findViewById(R.id.watchListCount)

        val helpIcon = view.findViewById<View>(R.id.helpIcon)

        // Help menu
        helpIcon.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), helpIcon)

            popupMenu.menuInflater.inflate(
                R.menu.profile_help_menu,
                popupMenu.menu
            )

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_help_center -> {
                        showHelpDialog()
                        true
                    }

                    R.id.menu_contact_us -> {
                        showContactDialog()
                        true
                    }

                    else -> false
                }
            }

            popupMenu.show()
        }

        // Load profile information
        loadProfile()
        loadWatchlistCount()
        loadProfileImage()
        loadOrderCount()

        // Check user roles
        checkAdminUser()
        checkSellerUser()

        // Edit Profile - existing functionality
        menuEditProfile.setOnClickListener {
            openEditProfileDialog()
        }

        // Change profile photo
        profileImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        // My Orders
        myOrdersButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, OrderFragment())
                .addToBackStack(null)
                .commit()
        }

        // Admin Orders
        adminOrdersButton.setOnClickListener {
            startActivity(
                Intent(requireContext(), AdminOrderActivity::class.java)
            )
        }

        // Seller Panel
        openSellerPanelButton.setOnClickListener {
            startActivity(
                Intent(requireContext(), SellerActivity::class.java)
            )
        }

        // Shipping
        shippingDetailsButton.setOnClickListener {
            startActivity(
                Intent(requireContext(), ShippingActivity::class.java)
            )
        }

        // Watchlist
        watchList.setOnClickListener {
            startActivity(
                Intent(requireContext(), WatchlistActivity::class.java)
            )
        }

        // My Payments
        view.findViewById<Button>(R.id.paymentButton)
            .setOnClickListener {
                showPaymentsDialog()
            }

        // All Coupons
        view.findViewById<Button>(R.id.allCouponsButton)
            .setOnClickListener {
                showCouponsDialog()
            }

        // My Rating and Reviews
        view.findViewById<Button>(R.id.ratingReviewButton)
            .setOnClickListener {
                showReviewsDialog()
            }

        // Settings
        view.findViewById<Button>(R.id.settingButton)
            .setOnClickListener {
                showSettingsDialog()
            }

        // Return and Refund Policy
        view.findViewById<Button>(R.id.policyButton)
            .setOnClickListener {
                showPolicyDialog()
            }

        // About Valmaro
        view.findViewById<Button>(R.id.aboutButton)
            .setOnClickListener {
                showAboutDialog()
            }

        // Logout - existing functionality
        logoutButton.setOnClickListener {
            auth.signOut()

            Toast.makeText(
                requireContext(),
                "Logged out successfully",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(
                requireContext(),
                SignInActivity::class.java
            )

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }

        return view
    }

    // =========================
    // MY PAYMENTS
    // =========================

    private fun showPaymentsDialog() {
        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(
                requireContext(),
                "Please login first",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        FirebaseDatabase.getInstance()
            .getReference("orders")
            .child(user.uid)
            .get()
            .addOnSuccessListener { snapshot ->

                if (!isAdded) return@addOnSuccessListener

                val paymentDetails = StringBuilder()

                for (order in snapshot.children) {
                    val productName =
                        order.child("productName")
                            .getValue(String::class.java) ?: "Product"

                    val price =
                        order.child("price")
                            .getValue(String::class.java) ?: "0"

                    val orderId =
                        order.child("orderId")
                            .getValue(String::class.java) ?: order.key ?: ""

                    paymentDetails.append(
                        "Order ID: $orderId\n" +
                                "Product: $productName\n" +
                                "Amount: ₹$price\n\n"
                    )
                }

                val message = if (paymentDetails.isEmpty()) {
                    "No order/payment records found."
                } else {
                    paymentDetails.toString()
                }

                AlertDialog.Builder(requireContext())
                    .setTitle("My Payments")
                    .setMessage(message)
                    .setPositiveButton("Close", null)
                    .show()
            }
            .addOnFailureListener { error ->
                if (!isAdded) return@addOnFailureListener

                Toast.makeText(
                    requireContext(),
                    "Error: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    // =========================
    // ALL COUPONS
    // =========================

    private fun showCouponsDialog() {
        val coupons = arrayOf(
            "WELCOME10 - 10% off",
            "VALMARO50 - ₹50 off",
            "SHOP20 - 20% off"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Available Coupons")
            .setItems(coupons) { _, position ->

                val selectedCoupon = when (position) {
                    0 -> "WELCOME10"
                    1 -> "VALMARO50"
                    else -> "SHOP20"
                }

                AlertDialog.Builder(requireContext())
                    .setTitle("Coupon Selected")
                    .setMessage(
                        "$selectedCoupon\n\n" +
                                "You can use this coupon when " +
                                "coupon support is connected to checkout."
                    )
                    .setPositiveButton("OK", null)
                    .show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    // =========================
    // RATINGS AND REVIEWS
    // =========================

    private fun showReviewsDialog() {
        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(
                requireContext(),
                "Please login first",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        FirebaseDatabase.getInstance()
            .getReference("reviews")
            .child(user.uid)
            .get()
            .addOnSuccessListener { snapshot ->

                if (!isAdded) return@addOnSuccessListener

                val reviews = StringBuilder()

                for (review in snapshot.children) {
                    val product =
                        review.child("productName")
                            .getValue(String::class.java) ?: "Product"

                    val rating =
                        review.child("rating")
                            .getValue(String::class.java)
                            ?: review.child("rating")
                                .getValue(Int::class.java)?.toString()
                            ?: "Not rated"

                    val comment =
                        review.child("comment")
                            .getValue(String::class.java) ?: ""

                    reviews.append(
                        "Product: $product\n" +
                                "Rating: $rating/5\n" +
                                "Review: $comment\n\n"
                    )
                }

                val message = if (reviews.isEmpty()) {
                    "You have not submitted any reviews yet."
                } else {
                    reviews.toString()
                }

                AlertDialog.Builder(requireContext())
                    .setTitle("My Rating and Reviews")
                    .setMessage(message)
                    .setPositiveButton("Close", null)
                    .show()
            }
            .addOnFailureListener { error ->
                if (!isAdded) return@addOnFailureListener

                Toast.makeText(
                    requireContext(),
                    "Error: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    // =========================
    // SETTINGS
    // =========================

    private fun showSettingsDialog() {
        val options = arrayOf(
            "Notifications",
            "Language",
            "Privacy and Security",
            "About Valmaro"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Settings")
            .setItems(options) { _, position ->
                when (position) {
                    0 -> showNotificationSettings()
                    1 -> showAboutDialog()
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showNotificationSettings() {
        val preferences = requireContext()
            .getSharedPreferences(
                "valmaro_settings",
                android.content.Context.MODE_PRIVATE
            )

        val isEnabled = preferences.getBoolean(
            "notifications_enabled",
            true
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Notifications")
            .setMessage(
                "Notifications are currently " +
                        if (isEnabled) "enabled." else "disabled."
            )
            .setPositiveButton(
                if (isEnabled) "Turn Off" else "Turn On"
            ) { _, _ ->

                preferences.edit()
                    .putBoolean(
                        "notifications_enabled",
                        !isEnabled
                    )
                    .apply()

                Toast.makeText(
                    requireContext(),
                    if (isEnabled) {
                        "Notifications preference disabled"
                    } else {
                        "Notifications preference enabled"
                    },
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // =========================
    // RETURN AND REFUND POLICY
    // =========================

    private fun showPolicyDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Return and Refund Policy")
            .setMessage(
                "Return and Refund Policy\n\n" +
                        "1. Return requests are subject to product " +
                        "eligibility.\n\n" +
                        "2. Products must meet the applicable " +
                        "return conditions.\n\n" +
                        "3. Refunds are processed according to " +
                        "the payment method and applicable terms." +
                        "and return will be 24 hours of product Pickups" +
                        "\n\n" +
                        "4. Contact customer support for assistance.\n\n" +
                        "Note: Replace this sample text with " +
                        "your actual business policy."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    // =========================
    // ABOUT VALMARO
    // =========================

    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("About Valmaro")
            .setMessage(
                "Valmaro\n\n" +
                        "Smart Shopping, Better Living\n\n" +
                        "Valmaro is a smart e-commerce application " +
                        "designed to make online shopping easier." +
                        "Thank You for visiting in the App\n\n" +
                        "Version 1.0"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    // =========================
    // HELP CENTER
    // =========================

    private fun showHelpDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Help Center")
            .setMessage(
                "Welcome to Valmaro Help Center.\n\n" +
                        "For assistance with orders, payments, " +
                        "or account-related issues, contact support."+"" +
                        "if the status is not success than wait 24 hours"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showContactDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Contact Us")
            .setMessage(
                "For support, please contact 9627316601 " +
                        "the Valmaro support team email : valmaro@gmail.com."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    // =========================
    // EDIT PROFILE
    // =========================

    private fun openEditProfileDialog() {
        val user = auth.currentUser ?: return

        val nameInput = EditText(requireContext())
        nameInput.hint = "Enter Name"
        nameInput.setText(profileName.text)

        val phoneInput = EditText(requireContext())
        phoneInput.hint = "Enter Phone"
        phoneInput.setText(profilePhone.text)

        val emailInput = EditText(requireContext())
        emailInput.hint = "Enter Email"
        emailInput.setText(profileEmail.text)

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 20, 40, 20)

        layout.addView(nameInput)
        layout.addView(phoneInput)
        layout.addView(emailInput)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->

                val name = nameInput.text.toString().trim()
                val phone = phoneInput.text.toString().trim()
                val email = emailInput.text.toString().trim()

                if (name.isEmpty() ||
                    phone.length != 10 ||
                    email.isEmpty()
                ) {
                    Toast.makeText(
                        requireContext(),
                        "Please enter valid details",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                val updates = mapOf(
                    "name" to name,
                    "phone" to phone,
                    "email" to email
                )

                FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.uid)
                    .updateChildren(updates)
                    .addOnSuccessListener {
                        if (!isAdded) return@addOnSuccessListener

                        profileName.text = name
                        profilePhone.text = phone
                        profileEmail.text = email

                        Toast.makeText(
                            requireContext(),
                            "Profile Updated Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { error ->
                        if (!isAdded) return@addOnFailureListener

                        Toast.makeText(
                            requireContext(),
                            "Error: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // =========================
    // WATCHLIST COUNT
    // =========================

    private fun loadWatchlistCount() {
        val user = auth.currentUser ?: return

        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(user.uid)
            .child("watchlist")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    watchListCount.text =
                        snapshot.childrenCount.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    if (!isAdded) return
                    watchListCount.text = "0"
                }
            })
    }

    // =========================
    // CHECK ADMIN
    // =========================

    private fun checkAdminUser() {
        val user = auth.currentUser

        if (user == null) {
            adminOrdersButton.visibility = View.GONE
            return
        }

        FirebaseDatabase.getInstance()
            .getReference("admins")
            .child(user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded) return@addOnSuccessListener

                val isAdmin =
                    snapshot.getValue(Boolean::class.java) == true

                adminOrdersButton.visibility =
                    if (isAdmin) View.VISIBLE else View.GONE
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                adminOrdersButton.visibility = View.GONE
            }
    }

    // =========================
    // CHECK SELLER
    // =========================

    private fun checkSellerUser() {
        val user = auth.currentUser

        if (user == null) {
            openSellerPanelButton.visibility = View.GONE
            return
        }

        FirebaseDatabase.getInstance()
            .getReference("sellers")
            .child(user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded) return@addOnSuccessListener

                val isSeller =
                    snapshot.getValue(Boolean::class.java) == true

                openSellerPanelButton.visibility =
                    if (isSeller) View.VISIBLE else View.GONE
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                openSellerPanelButton.visibility = View.GONE
            }
    }

    // =========================
    // LOAD PROFILE IMAGE
    // =========================

    private fun loadProfileImage() {
        val user = auth.currentUser ?: return

        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(user.uid)
            .child("profileImage")
            .get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded) return@addOnSuccessListener

                val imageUrl =
                    snapshot.getValue(String::class.java)

                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(
                            android.R.drawable.ic_menu_myplaces
                        )
                        .error(
                            android.R.drawable.ic_menu_myplaces
                        )
                        .circleCrop()
                        .into(profileImage)
                }
            }
    }

    // =========================
    // UPLOAD PROFILE IMAGE
    // =========================

    private fun uploadProfileImage(uri: Uri) {
        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(
                requireContext(),
                "Please login first",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val uid = user.uid

        val storageRef = FirebaseStorage.getInstance()
            .getReference("profile_images")
            .child(uid)

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl
                    .addOnSuccessListener { downloadUri ->

                        FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid)
                            .child("profileImage")
                            .setValue(downloadUri.toString())
                            .addOnSuccessListener {
                                if (!isAdded) return@addOnSuccessListener

                                Toast.makeText(
                                    requireContext(),
                                    "Profile photo saved!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
            }
            .addOnFailureListener { error ->
                if (!isAdded) return@addOnFailureListener

                Toast.makeText(
                    requireContext(),
                    "Upload failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    // =========================
    // LOAD ORDER COUNT
    // =========================

    private fun loadOrderCount() {
        val user = auth.currentUser ?: return

        FirebaseDatabase.getInstance()
            .getReference("orders")
            .child(user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded) return@addOnSuccessListener
                orderCountText.text =
                    snapshot.childrenCount.toString()
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                orderCountText.text = "0"
            }
    }

    // =========================
    // LOAD PROFILE
    // =========================

    private fun loadProfile() {
        val user = auth.currentUser ?: return
        val authEmail = user.email ?: ""

        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded) return@addOnSuccessListener

                val name = snapshot.child("name")
                    .getValue(String::class.java)

                val phone = snapshot.child("phone")
                    .getValue(String::class.java)

                val email = snapshot.child("email")
                    .getValue(String::class.java)

                profileName.text =
                    name ?: user.displayName ?: ""

                profilePhone.text = phone ?: ""

                profileEmail.text =
                    email?.takeIf { it.isNotBlank() } ?: authEmail
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                profileEmail.text = authEmail
            }
    }
}
