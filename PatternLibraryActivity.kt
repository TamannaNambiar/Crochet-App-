package com.example.crochetappmanagement

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.crochetappmanagement.databinding.ActivityPatternLibraryBinding
import com.example.crochetappmanagement.databinding.BottomNavBarBinding

class PatternLibraryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPatternLibraryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatternLibraryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        setupPatternInteractions()
        setupBottomNavigation()
    }

    private fun setupPatternInteractions() {
        // Initialize SharedPreferences to save the "Favorite" state
        val sharedPref = getSharedPreferences("CrochetAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        // --- PATTERN 1: Cozy Baby Hat ---
        var isLiked1 = sharedPref.getBoolean("fav_hat", false)
        updateLikeVisuals(binding.btnLike1, isLiked1)

        binding.btnLike1.setOnClickListener {
            isLiked1 = !isLiked1
            updateLikeVisuals(binding.btnLike1, isLiked1)

            // Save to Favorites Module
            editor.putBoolean("fav_hat", isLiked1).apply()

            val message = if (isLiked1) "Added to Favorites" else "Removed from Favorites"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        binding.btnViewHat.setOnClickListener {
            Toast.makeText(this, "Opening Cozy Baby Hat details...", Toast.LENGTH_SHORT).show()
        }

        // --- PATTERN 2: Granny Square Blanket ---
        var isLiked2 = sharedPref.getBoolean("fav_granny", false)
        updateLikeVisuals(binding.btnLike2, isLiked2)

        binding.btnLike2.setOnClickListener {
            isLiked2 = !isLiked2
            updateLikeVisuals(binding.btnLike2, isLiked2)

            // Save to Favorites Module
            editor.putBoolean("fav_granny", isLiked2).apply()
        }

        binding.btnViewGranny.setOnClickListener {
            Toast.makeText(this, "Opening Granny Square details...", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper function to handle the heart UI changes
    private fun updateLikeVisuals(heartIcon: ImageView, isLiked: Boolean) {
        if (isLiked) {
            heartIcon.setImageResource(R.drawable.ic_heart_filled)
            heartIcon.imageTintList = ColorStateList.valueOf(Color.RED)
        } else {
            heartIcon.setImageResource(R.drawable.ic_heart_outline)
            heartIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#DBE9F4"))
        }
    }

    private fun setupBottomNavigation() {
        val navView = findViewById<View>(R.id.bottomNav)
        val nav = BottomNavBarBinding.bind(navView)

        nav.navHome.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        nav.navTutorials.setOnClickListener {
            startActivity(Intent(this, TutorialActivity::class.java))
        }

        nav.navPatterns.setOnClickListener {
            Toast.makeText(this, "You are already in Pattern Library", Toast.LENGTH_SHORT).show()
        }

        nav.navProjects.setOnClickListener {
            startActivity(Intent(this, ProjectTrackerActivity::class.java))
        }

        nav.navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}