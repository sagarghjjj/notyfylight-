package com.sagar.notifylight

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import java.util.concurrent.atomic.AtomicBoolean

class FlashHelper(private val context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var frontFlashId: String? = null
    private var maxStrengthLevel: Int = 1
    private val isBlinking = AtomicBoolean(false)

    init {
        for (id in cameraManager.cameraIdList) {
            val chars = cameraManager.getCameraCharacteristics(id)
            val facing = chars.get(CameraCharacteristics.LENS_FACING)
            val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
            if (facing == CameraCharacteristics.LENS_FACING_FRONT && hasFlash) {
                frontFlashId = id
                if (Build.VERSION.SDK_INT >= 33) {
                    maxStrengthLevel = chars.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
                }
            }
        }
    }

    fun supportsLowIntensity(): Boolean = Build.VERSION.SDK_INT >= 33 && maxStrengthLevel > 1

    private fun isLowIntensityEnabled(): Boolean {
        val prefs = context.getSharedPreferences("notifylight_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("low_intensity", false)
    }

    fun blink(times: Int = 5, onMs: Long = 300, offMs: Long = 300) {
        val id = frontFlashId ?: return
        if (!isBlinking.compareAndSet(false, true)) return

        val useLow = isLowIntensityEnabled() && supportsLowIntensity()

        Thread {
            try {
                repeat(times) {
                    if (useLow) {
                        cameraManager.turnOnTorchWithStrengthLevel(id, 1)
                    } else {
                        cameraManager.setTorchMode(id, true)
                    }
                    Thread.sleep(onMs)
                    cameraManager.setTorchMode(id, false)
                    Thread.sleep(offMs)
                }
            } finally {
                isBlinking.set(false)
            }
        }.start()
    }

    fun hasFrontFlash() = frontFlashId != null
}
