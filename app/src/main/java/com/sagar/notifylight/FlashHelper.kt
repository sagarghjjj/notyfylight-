package com.sagar.notifylight

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import java.util.concurrent.atomic.AtomicBoolean

class FlashHelper(private val context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var frontFlashId: String? = null
    private val isBlinking = AtomicBoolean(false)

    init {
        for (id in cameraManager.cameraIdList) {
            val chars = cameraManager.getCameraCharacteristics(id)
            val facing = chars.get(CameraCharacteristics.LENS_FACING)
            val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
            if (facing == CameraCharacteristics.LENS_FACING_FRONT && hasFlash) {
                frontFlashId = id
            }
        }
    }

    fun hasFrontFlash() = frontFlashId != null

    private fun getIntensity(): String {
        val prefs = context.getSharedPreferences("notifylight_prefs", Context.MODE_PRIVATE)
        return prefs.getString("intensity", "high") ?: "high"
    }

    fun blink(times: Int = 5, onMs: Long = 300, offMs: Long = 300) {
        val id = frontFlashId ?: return
        if (!isBlinking.compareAndSet(false, true)) return

        val intensity = getIntensity()

        Thread {
            try {
                repeat(times) {
                    when (intensity) {
                        "low" -> pulseFor(id, onMs, onTime = 8, offTime = 32)
                        "mid" -> pulseFor(id, onMs, onTime = 18, offTime = 12)
                        else -> {
                            // High = full solid brightness
                            cameraManager.setTorchMode(id, true)
                            Thread.sleep(onMs)
                            cameraManager.setTorchMode(id, false)
                        }
                    }
                    Thread.sleep(offMs)
                }
            } finally {
                isBlinking.set(false)
            }
        }.start()
    }

    private fun pulseFor(id: String, durationMs: Long, onTime: Long, offTime: Long) {
        val end = System.currentTimeMillis() + durationMs
        while (System.currentTimeMillis() < end) {
            cameraManager.setTorchMode(id, true)
            Thread.sleep(onTime)
            cameraManager.setTorchMode(id, false)
            Thread.sleep(offTime)
        }
    }
}
