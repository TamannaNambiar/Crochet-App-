package com.example.crochetappmanagement

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.crochetappmanagement.databinding.ActivityProfileBinding
import com.example.crochetappmanagement.databinding.BottomNavBarBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private var projectCount = 0
    private var tutorialCount = 0

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.ivUserImage.setImageURI(it)
            Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.profileContainer.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // --- FIXED: Linked the Reset Button directly ---
        binding.btnResetStats.setOnClickListener {
            showResetConfirmation()
        }

        // Optional: Keep long press on logout as a shortcut
        binding.btnLogout.setOnLongClickListener {
            showResetConfirmation()
            true
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val currentUser = auth.currentUser
        binding.tvProfileName.text = currentUser?.displayName ?: currentUser?.email?.substringBefore("@") ?: "User"

        initializeStatLabels()
        fetchUserStats()

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        setupBottomNavigation()
    }

    private fun initializeStatLabels() {
        val projectBox = binding.statProjects.root
        projectBox.findViewById<TextView>(R.id.tvStatLabel).text = "Projects"
        projectBox.findViewById<ImageView>(R.id.ivStatIcon).setImageResource(R.drawable.ic_project)

        val tutorialBox = binding.statTutorials.root
        tutorialBox.findViewById<TextView>(R.id.tvStatLabel).text = "Tutorials"
        tutorialBox.findViewById<ImageView>(R.id.ivStatIcon).setImageResource(R.drawable.ic_tutorial)
    }

    private fun fetchUserStats() {
        val userId = auth.currentUser?.uid ?: return

        database.child("Projects").orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    projectCount = snapshot.childrenCount.toInt()
                    // Target the specific TextView inside the included layout
                    binding.statProjects.root.findViewById<TextView>(R.id.tvStatValue).text = projectCount.toString()
                    updateOverallProgress()
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        database.child("Users").child(userId).child("CompletedTutorials")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    tutorialCount = snapshot.childrenCount.toInt()
                    // Target the specific TextView inside the included layout
                    binding.statTutorials.root.findViewById<TextView>(R.id.tvStatValue).text = tutorialCount.toString()
                    updateOverallProgress()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun showResetConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Reset All Progress?")
            .setMessage("This will set your Projects and Tutorials back to 0. This action cannot be undone.")
            .setPositiveButton("Reset") { _, _ -> resetStats() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun resetStats() {
        val userId = auth.currentUser?.uid ?: return

        // --- FIXED: Immediate UI feedback ---
        projectCount = 0
        tutorialCount = 0

        // Clear text views manually so they hit "0" instantly
        binding.statProjects.root.findViewById<TextView>(R.id.tvStatValue).text = "0"
        binding.statTutorials.root.findViewById<TextView>(R.id.tvStatValue).text = "0"
        updateOverallProgress()

        // 1. Delete all Projects for this user in Firebase
        database.child("Projects").orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        child.ref.removeValue()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        // 2. Delete all Completed Tutorials for this user in Firebase
        database.child("Users").child(userId).child("CompletedTutorials").removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Progress Reset to 0", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateOverallProgress() {
        val levelGoal = 20
        val totalFinished = projectCount + tutorialCount
        val progressPercent = if (totalFinished > 0) (totalFinished * 100) / levelGoal else 0
        val finalProgress = if (progressPercent > 100) 100 else progressPercent

        binding.circularProgress.progress = finalProgress
        binding.tvPercentCircle.text = "$finalProgress%"
        binding.horizontalGoalBar.progress = finalProgress
        binding.tvGoalSubtext.text = "$finalProgress% of Level 1 (Goal: $levelGoal)"
    }

    private fun setupBottomNavigation() {
        val navView = binding.bottomNav.root
        val nav = BottomNavBarBinding.bind(navView)

        val clickListener = View.OnClickListener { view ->
            val targetActivity = when (view.id) {
                R.id.navHome -> DashboardActivity::class.java
                R.id.navTutorials -> TutorialActivity::class.java
                R.id.navPatterns -> PatternLibraryActivity::class.java
                R.id.navProjects -> ProjectTrackerActivity::class.java
                else -> null
            }
            targetActivity?.let {
                startActivity(Intent(this, it).apply {
                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                })
            }
        }

        nav.navHome.setOnClickListener(clickListener)
        nav.navTutorials.setOnClickListener(clickListener)
        nav.navPatterns.setOnClickListener(clickListener)
        nav.navProjects.setOnClickListener(clickListener)

        nav.navProfile.visibility = View.VISIBLE
        nav.navProfile.alpha = 1.0f
    }
}