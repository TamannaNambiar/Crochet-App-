package com.example.crochetappmanagement

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.crochetappmanagement.databinding.ActivityDashboardBinding
import com.example.crochetappmanagement.databinding.BottomNavBarBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var auth: FirebaseAuth
    private var currentProjectId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // 1. FIX FOR PHONE OVERLAP (Pushes UI above Home/Back buttons)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fetchRecentProject()
        setupNavigation()
        setupDeleteLogic()
    }

    private fun fetchRecentProject() {
        val database = FirebaseDatabase.getInstance().getReference("Projects")

        database.limitToLast(1).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (projectSnapshot in snapshot.children) {
                        currentProjectId = projectSnapshot.child("id").value.toString()
                        val name = projectSnapshot.child("name").value.toString()
                        val status = projectSnapshot.child("status").value.toString()

                        binding.tvProjectNameCard.text = name
                        binding.tvProjectStatusCard.text = "Status: $status"
                        binding.btnDeleteProject.visibility = View.VISIBLE
                    }
                } else {
                    binding.tvProjectNameCard.text = "No active project"
                    binding.tvProjectStatusCard.text = "Start a new one in Projects"
                    binding.btnDeleteProject.visibility = View.GONE
                    currentProjectId = null
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DashboardActivity, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupDeleteLogic() {
        binding.btnDeleteProject.setOnClickListener {
            val database = FirebaseDatabase.getInstance().getReference("Projects")
            currentProjectId?.let { id ->
                database.child(id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this@DashboardActivity, "Project deleted!", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun setupNavigation() {
        // --- TOP BAR ICONS ---
        binding.ivBell.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        binding.btnBack.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // --- GRID CARDS (Quick Actions) ---
        binding.btnProjectsCard.setOnClickListener {
            startActivity(Intent(this, ProjectTrackerActivity::class.java))
        }

        binding.btnTutorialsCard.setOnClickListener {
            startActivity(Intent(this, TutorialActivity::class.java))
        }

        binding.btnPatternsCard.setOnClickListener {
            startActivity(Intent(this, PatternLibraryActivity::class.java))
        }

        binding.btnFavoritesCard.setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        binding.btnCalculatorCard.setOnClickListener {
            startActivity(Intent(this, CrochetCalculatorActivity::class.java))
        }

        // --- INITIALIZE BOTTOM NAVIGATION ---
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        // This binds the shared bottom navigation layout
        val navView = findViewById<View>(R.id.bottomNav)
        val nav = BottomNavBarBinding.bind(navView)

        nav.navHome.setOnClickListener {
            // Already on Dashboard, just scroll to top or refresh
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
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