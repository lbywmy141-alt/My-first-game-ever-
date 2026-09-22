package com.example.game.engine

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class VibrationManager(private val context: Context) {
    var isEnabled: Boolean = true

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun vibrateHit() {
        if (!isEnabled) return
        vibrate(35L, 120)
    }

    fun vibrateHeavyImpact() {
        if (!isEnabled) return
        vibrate(70L, 220)
    }

    fun vibrateParry() {
        if (!isEnabled) return
        vibrate(45L, 255)
    }

    fun vibrateBossSlam() {
        if (!isEnabled) return
        vibrate(120L, 255)
    }

    private fun vibrate(durationMs: Long, amplitude: Int) {
        try {
            vibrator?.let { v ->
                if (!v.hasVibrator()) return
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255))
                    v.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }
}
