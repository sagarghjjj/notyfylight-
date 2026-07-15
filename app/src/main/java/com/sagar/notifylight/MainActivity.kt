package com.sagar.notifylight

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var flashHelper: FlashHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        flashHelper = FlashHelper(this)

        findViewById<Button>(R.id.btnEnable).setOnClickListener {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }

        val prefs = getSharedPreferences("notifylight_prefs", Context.MODE_PRIVATE)
        val seekBar = findViewById<SeekBar>(R.id.seekBrightness)
        val label = findViewById<TextView>(R.id.labelBrightness)

        val saved = prefs.getInt("brightness", 100)
        seekBar.progress = saved
        label.text = "Brightness: $saved%"

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress.coerceAtLeast(1)
                label.text = "Brightness: $value%"
                prefs.edit().putInt("brightness", value).apply()

                if (fromUser) {
                    flashHelper.updatePreviewBrightness(value)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (flashHelper.hasFrontFlash()) {
                    flashHelper.startPreview(seekBar?.progress?.coerceAtLeast(1) ?: 100)
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                flashHelper.stopPreview()
            }
        })
    }
}
