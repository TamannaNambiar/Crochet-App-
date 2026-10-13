package com.example.crochetappmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.crochetappmanagement.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.etFullName.addTextChangedListener { binding.etFullName.error = null }
        binding.etPassword.addTextChangedListener { binding.etPassword.error = null }
        binding.etConfirmPassword.addTextChangedListener { binding.etConfirmPassword.error = null }

        binding.btnRegister.setOnClickListener {
            val name = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            // 1. Basic Empty Check
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Name Validation
            val namePattern = "^[a-zA-Z\\s]*$".toRegex()
            if (!name.matches(namePattern)) {
                binding.etFullName.error = "Name must only contain letters"
                return@setOnClickListener
            }

            // 3. Password Length Check (Firebase requires at least 6)
            if (password.length < 6) {
                binding.etPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            // 4. Match Check
            if (password != confirmPassword) {
                binding.etConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            }

            // 5. Create User in Firebase
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val userId = user?.uid

                        val profileUpdates = userProfileChangeRequest {
                            displayName = name
                        }

                        user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                val database = FirebaseDatabase.getInstance().getReference("Users")

                                val userMap = mapOf(
                                    "name" to name,
                                    "email" to email,
                                    "projectsCompleted" to 0,
                                    "tutorialsFinished" to 0,
                                    "level" to "Beginner"
                                )

                                userId?.let { id ->
                                    database.child(id).setValue(userMap)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Registration Successful! Please Login.", Toast.LENGTH_LONG).show()

                                            // REDIRECT TO LOGIN PAGE
                                            val intent = Intent(this, LoginActivity::class.java)
                                            // Clear activity stack so user can't go back to register screen
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this, "Database Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        }
                    } else {
                        // Most common error here is "Email already in use"
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        binding.tvBackToLogin.setOnClickListener {
            finish()
        }
    }
}