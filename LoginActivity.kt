package com.example.crochetappmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.crochetappmanagement.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // OPTIONAL: Check if an email was passed from RegisterActivity to auto-fill
        val registeredEmail = intent.getStringExtra("REGISTERED_EMAIL")
        if (!registeredEmail.isNullOrEmpty()) {
            binding.etEmail.setText(registeredEmail)
        }

        // Clear the error message automatically when the user starts typing again
        binding.etPassword.addTextChangedListener {
            binding.etPassword.error = null
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // 1. Validation: Check for empty fields
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Removed strict 15-character check to match the new Registration rules.
            // Firebase requires at least 6 characters.
            if (password.length < 6) {
                binding.etPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            // 3. Firebase Login
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Welcome Back!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Shows the specific Firebase error (e.g., wrong password or no user)
                        Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        // Stay logged in: if user is already signed in, skip login screen
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}