package com.example.crochetappmanagement

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.crochetappmanagement.databinding.ActivityProjectTrackerBinding
import com.example.crochetappmanagement.databinding.BottomNavBarBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class ProjectTrackerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectTrackerBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectTrackerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // 1. FIX FOR PHONE OVERLAP
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupSpinners()
        setupDatePicker()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSaveProject.setOnClickListener {
            saveProjectToFirebase()
        }

        setupBottomNavigation()
    }

    private fun saveProjectToFirebase() {
        val projectName = binding.etProjectName.text.toString().trim()
        val pattern = binding.spinnerPattern.selectedItem.toString()
        val startDate = binding.etStartDate.text.toString().trim()
        val status = binding.spinnerStatus.selectedItem.toString()

        val userId = auth.currentUser?.uid

        if (projectName.isEmpty()) {
            binding.etProjectName.error = "Please enter a project name"
            return
        }

        if (userId == null) {
            Toast.makeText(this, "Please login to save projects", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance().getReference("Projects")
        val projectId = database.push().key ?: return

        val projectData = mapOf(
            "id" to projectId,
            "userId" to userId,
            "name" to projectName,
            "pattern" to pattern,
            "date" to startDate,
            "status" to status
        )

        database.child(projectId).setValue(projectData)
            .addOnSuccessListener {
                Toast.makeText(this, "Project '$projectName' Saved!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ProfileActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Save Failed. Check Internet.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBottomNavigation() {
        // Changed binding approach slightly for better compatibility with your include ID
        val nav = BottomNavBarBinding.bind(binding.bottomNav.root)

        // HOME
        nav.navHome.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        // TUTORIALS
        nav.navTutorials.setOnClickListener {
            val intent = Intent(this, TutorialActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        // PATTERNS
        nav.navPatterns.setOnClickListener {
            val intent = Intent(this, PatternLibraryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        // PROJECTS (Already here)
        nav.navProjects.setOnClickListener {
            // Stay here
        }

        // --- UPDATED: PROFILE ICON IS NOW VISIBLE AND FUNCTIONAL ---
        nav.navProfile.visibility = View.VISIBLE
        nav.navProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }
    }

    private fun setupSpinners() {
        val patterns = arrayOf("Select Pattern", "Bandana", "Scarf", "Bucket Hat", "Amigurumi")
        val patternAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, patterns)
        binding.spinnerPattern.adapter = patternAdapter

        val statuses = arrayOf("Ongoing", "Completed", "On Hold", "Planned")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statuses)
        binding.spinnerStatus.adapter = statusAdapter
    }

    private fun setupDatePicker() {
        binding.etStartDate.setOnClickListener {
            val c = Calendar.getInstance()
            val dpd = DatePickerDialog(this, { _, year, month, day ->
                binding.etStartDate.setText("$day/${month + 1}/$year")
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
            dpd.show()
        }
    }
}