package com.example.crochetappmanagement

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.crochetappmanagement.databinding.ActivityFavoritesBinding
import com.example.crochetappmanagement.databinding.BottomNavBarBinding

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnBack.setOnClickListener { finish() }


        loadUserFavorites()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        loadUserFavorites()
    }

    private fun loadUserFavorites() {
        val sharedPref = getSharedPreferences("CrochetAppPrefs", Context.MODE_PRIVATE)

        val isHatFav = sharedPref.getBoolean("fav_hat", false)
        val isGrannyFav = sharedPref.getBoolean("fav_granny", false)

        binding.favCardHat.visibility = if (isHatFav) View.VISIBLE else View.GONE
        binding.favCardGranny.visibility = if (isGrannyFav) View.VISIBLE else View.GONE

        if (!isHatFav && !isGrannyFav) {
            binding.tvEmptyMessage.visibility = View.VISIBLE
        } else {
            binding.tvEmptyMessage.visibility = View.GONE
        }
    }

    private fun setupBottomNavigation() {
        val nav = BottomNavBarBinding.bind(binding.bottomNav.root)

        nav.navHome.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        nav.navTutorials.setOnClickListener {
            startActivity(Intent(this, TutorialActivity::class.java))
        }

        nav.navPatterns.setOnClickListener {
            startActivity(Intent(this, PatternLibraryActivity::class.java))
        }

        nav.navProjects.setOnClickListener {
            startActivity(Intent(this, ProjectTrackerActivity::class.java))
        }

        nav.navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}