package com.sagar.notifylight

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

class FlashHelper(context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var frontFlashId: String? = null

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

    fun blink(times: Int = 5, onMs: Long = 300, offMs: Long = 300) {
        val id = frontFlashId ?: return
        Thread {
            repeat(times) {
                cameraManager.setTorchMode(id, true)
                Thread.sleep(onMs)
                cameraManager.setTorchMode(id, false)
                Thread.sleep(offMs)
            }
        }.start()
    }

    fun hasFrontFlash() = frontFlashId != null
}
