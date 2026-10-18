package com.example.valmaro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check user login
        if (FirebaseAuth.getInstance().currentUser == null) {

            startActivity(
                Intent(this, SignInActivity::class.java)
            )

            finish()
            return
        }

        setContentView(R.layout.activity_main)

        CartManager.init(this)

        // Bottom Navigation
        bottomNavigation = findViewById(R.id.bottomNavigation)

        // App open hone par Home Fragment
        if (savedInstanceState == null) {
            openFragment(HomeFragment())
        }

        // Bottom Navigation click
        bottomNavigation.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.home -> {
                    openFragment(HomeFragment())
                    true
                }

                R.id.cart -> {
                    openFragment(CartFragment())
                    true
                }

                R.id.order -> {
                    openFragment(OrderFragment())
                    true
                }

                R.id.profile -> {
                    openFragment(ProfileFragment())
                    true
                }

                R.id.category -> {
                    openFragment(CategorieFragment())
                    true
                }

                else -> false
            }
        }
    }

    private fun openFragment(fragment: Fragment) {

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.frameLayout,
                fragment
            )
            .commit()
    }
}