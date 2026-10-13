package com.example.crochetappmanagement

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.crochetappmanagement.databinding.ActivityCrochetCalculatorBinding
import com.example.crochetappmanagement.databinding.BottomNavBarBinding

class CrochetCalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrochetCalculatorBinding
    private var chainCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrochetCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. FIX FOR PHONE OVERLAP (System Navigation Bar Padding)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 2. BACK BUTTON
        binding.btnBack.setOnClickListener {
            finish()
        }

        // 3. STITCH CALCULATOR LOGIC
        binding.btnCalculate.setOnClickListener {
            calculateStitches()
        }

        // 4. LIVE CHAIN COUNTER LOGIC
        binding.btnPlus.setOnClickListener {
            chainCount++
            binding.tvCurrentCount.text = chainCount.toString()
        }

        binding.btnReset.setOnClickListener {
            chainCount = 0
            binding.tvCurrentCount.text = "0"
        }

        // 5. INITIALIZE SHARED BOTTOM NAVIGATION
        setupBottomNavigation()
    }

    private fun calculateStitches() {
        val widthStr = binding.etWidth.text.toString()
        val gaugeStr = binding.etGauge.text.toString()

        if (widthStr.isNotEmpty() && gaugeStr.isNotEmpty()) {
            val width = widthStr.toDouble()
            val gauge = gaugeStr.toDouble()

            // Formula: Width * Stitches per cm
            val result = (width * gauge).toInt()
            binding.tvResult.text = "$result stitches"

            Toast.makeText(this, "Calculation Complete", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please enter Width and Gauge", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        // Safe binding using the ID of the include tag in your XML
        val navView = findViewById<View>(R.id.bottomNav)
        val nav = BottomNavBarBinding.bind(navView)

        // HOME -> Dashboard
        nav.navHome.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        // TUTORIALS
        nav.navTutorials.setOnClickListener {
            startActivity(Intent(this, TutorialActivity::class.java))
        }

        // PATTERNS
        nav.navPatterns.setOnClickListener {
            startActivity(Intent(this, PatternLibraryActivity::class.java))
        }

        // PROJECTS
        nav.navProjects.setOnClickListener {
            startActivity(Intent(this, ProjectTrackerActivity::class.java))
        }

        // PROFILE
        nav.navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}