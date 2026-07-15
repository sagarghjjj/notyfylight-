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

    private fun getBrightness(): Int {
        val prefs = context.getSharedPreferences("notifylight_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("brightness", 100)
    }

    fun blink(times: Int = 5, onMs: Long = 300, offMs: Long = 300) {
        val id = frontFlashId ?: return
        if (!isBlinking.compareAndSet(false, true)) return

        val brightness = getBrightness().coerceIn(1, 100)

        Thread {
            try {
                repeat(times) {
                    if (brightness >= 95) {
                        // Near-max: just solid on, no pulsing needed
                        cameraManager.setTorchMode(id, true)
                        Thread.sleep(onMs)
                        cameraManager.setTorchMode(id, false)
                    } else {
                        pulseFor(id, onMs, brightness)
                    }
                    Thread.sleep(offMs)
                }
            } finally {
                isBlinking.set(false)
            }
        }.start()
    }

    // Simulates brightness by rapidly pulsing on/off with a duty cycle
    // proportional to the brightness percentage.
    private fun pulseFor(id: String, durationMs: Long, brightness: Int) {
        val cycleMs = 40L
        val onTime = ((brightness / 100.0) * cycleMs).toLong().coerceAtLeast(2)
        val offTime = (cycleMs - onTime).coerceAtLeast(2)

        val end = System.currentTimeMillis() + durationMs
        while (System.currentTimeMillis() < end) {
            cameraManager.setTorchMode(id, true)
            Thread.sleep(onTime)
            cameraManager.setTorchMode(id, false)
            Thread.sleep(offTime)
        }
    }
}
