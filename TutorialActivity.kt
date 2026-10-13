package com.example.crochetappmanagement

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.crochetappmanagement.databinding.ActivityTutorialBinding
import com.example.crochetappmanagement.databinding.BottomNavBarBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TutorialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTutorialBinding
    private lateinit var auth: FirebaseAuth

    private var currentUrl = "https://www.youtube.com/watch?v=IE0r4YChFA8"
    private var currentTutorialName = "BEGINNER BANDANA"
    private var currentTutorialId = "tutorial_beginner"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.imgTutorialCover.setOnClickListener { openYoutubeVideo() }
        binding.btnBack.setOnClickListener { finish() }

        // --- TOGGLE LOGIC: Save or Undo ---
        binding.btnDone.setOnClickListener {
            val isAlreadyDone = binding.btnDone.text.toString().contains("Undo", ignoreCase = true)
            if (isAlreadyDone) {
                undoTutorialProgress()
            } else {
                saveProgressAndOpenLink()
            }
        }

        // --- TAB SELECTION LOGIC ---
        binding.tabBeginner.setOnClickListener {
            updateTutorialSelection("https://www.youtube.com/watch?v=IE0r4YChFA8",
                "BEGINNER BANDANA", "tutorial_beginner", R.drawable.tutorial_beginner)
        }

        binding.tabIntermediate.setOnClickListener {
            updateTutorialSelection("https://www.youtube.com/watch?v=mYKclDCKCg4",
                "INTERMEDIATE SHORTS", "tutorial_intermediate", R.drawable.tutorial_intermediate)
        }

        binding.tabAdvanced.setOnClickListener {
            updateTutorialSelection("https://www.youtube.com/watch?v=L5KB232ndAc",
                "ADVANCED LEAF TOP", "tutorial_advanced", R.drawable.tutorial_advanced)
        }

        checkIfAlreadyDone()
        setupBottomNavigation()
    }

    // Helper to reduce code repetition when switching tabs
    private fun updateTutorialSelection(url: String, name: String, id: String, imageRes: Int) {
        currentUrl = url
        currentTutorialName = name
        currentTutorialId = id
        binding.imgTutorialCover.setImageResource(imageRes)
        binding.tvTutorialTitle.text = name
        checkIfAlreadyDone() // Refresh button text for the new selection
    }

    private fun checkIfAlreadyDone() {
        val userId = auth.currentUser?.uid ?: return
        val tutorialRef = FirebaseDatabase.getInstance().getReference("Users")
            .child(userId).child("CompletedTutorials").child(currentTutorialId)

        tutorialRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.btnDone.text = if (snapshot.exists()) "Undo Completion" else "Mark as Done"
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun undoTutorialProgress() {
        val userId = auth.currentUser?.uid ?: return
        val tutorialRef = FirebaseDatabase.getInstance().getReference("Users")
            .child(userId).child("CompletedTutorials").child(currentTutorialId)

        tutorialRef.removeValue().addOnSuccessListener {
            Toast.makeText(this, "Progress Undone", Toast.LENGTH_SHORT).show()
            binding.btnDone.text = "Mark as Done"
        }
    }

    private fun saveProgressAndOpenLink() {
        val userId = auth.currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().getReference("Users")
            .child(userId).child("CompletedTutorials")

        val tutorialData = mapOf(
            "name" to currentTutorialName,
            "url" to currentUrl,
            "status" to "Completed"
        )

        database.child(currentTutorialId).setValue(tutorialData)
            .addOnSuccessListener {
                Toast.makeText(this, "Progress Saved!", Toast.LENGTH_SHORT).show()
                binding.btnDone.text = "Undo Completion"
                openYoutubeVideo()
            }
    }

    private fun openYoutubeVideo() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
        intent.setPackage("com.google.android.youtube")
        try {
            startActivity(intent)
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl)))
        }
    }

    private fun setupBottomNavigation() {
        val nav = BottomNavBarBinding.bind(binding.bottomNav.root)
        val clickListener = View.OnClickListener { view ->
            val target = when (view.id) {
                R.id.navHome -> DashboardActivity::class.java
                R.id.navPatterns -> PatternLibraryActivity::class.java
                R.id.navProjects -> ProjectTrackerActivity::class.java
                R.id.navProfile -> ProfileActivity::class.java
                else -> null
            }
            target?.let {
                startActivity(Intent(this, it).apply { flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT })
            }
        }
        nav.navHome.setOnClickListener(clickListener)
        nav.navPatterns.setOnClickListener(clickListener)
        nav.navProjects.setOnClickListener(clickListener)
        nav.navProfile.setOnClickListener(clickListener)
        nav.navProfile.visibility = View.VISIBLE
    }
}