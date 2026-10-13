package com.example.crochetappmanagement

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
// This must match your XML file name: activity_notification.xml -> ActivityNotificationBinding
import com.example.crochetappmanagement.databinding.ActivityNotificationBinding

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the binding
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Back button functionality
        binding.btnBack.setOnClickListener {
            finish()
        }

        // 2. Handle the switch toggle for reminders
        binding.switchReminders.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Daily Reminders Enabled" else "Daily Reminders Disabled"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}