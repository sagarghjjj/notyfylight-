package com.sagar.notifylight

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.Switch
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnEnable).setOnClickListener {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }

        val flashHelper = FlashHelper(this)
        val prefs = getSharedPreferences("notifylight_prefs", Context.MODE_PRIVATE)
        val lowIntensitySwitch = findViewById<Switch>(R.id.switchLowIntensity)

        lowIntensitySwitch.isChecked = prefs.getBoolean("low_intensity", false)

        if (!flashHelper.supportsLowIntensity()) {
            lowIntensitySwitch.isEnabled = false
            Toast.makeText(this, "Your device doesn't support adjustable flash brightness", Toast.LENGTH_LONG).show()
        }

        lowIntensitySwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("low_intensity", isChecked).apply()
        }
    }
}
