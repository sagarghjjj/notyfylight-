package com.sagar.notifylight

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.RadioGroup

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnEnable).setOnClickListener {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }

        val prefs = getSharedPreferences("notifylight_prefs", Context.MODE_PRIVATE)
        val radioGroup = findViewById<RadioGroup>(R.id.radioIntensity)

        // Set initial selection based on saved preference
        when (prefs.getString("intensity", "high")) {
            "low" -> radioGroup.check(R.id.radioLow)
            "mid" -> radioGroup.check(R.id.radioMid)
            else -> radioGroup.check(R.id.radioHigh)
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val value = when (checkedId) {
                R.id.radioLow -> "low"
                R.id.radioMid -> "mid"
                else -> "high"
            }
            prefs.edit().putString("intensity", value).apply()
        }
    }
}
