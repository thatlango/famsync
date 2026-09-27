package com.example.util

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

object SoundEffectsManager {
    private var toneGen: ToneGenerator? = null

    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            Log.e("SoundEffectsManager", "Failed to initialize ToneGenerator", e)
        }
    }

    fun playSuccessChime() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        } catch (e: Exception) {
            Log.e("SoundEffectsManager", "Error playing chime", e)
        }
    }

    fun playStarCelebration() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 250)
        } catch (e: Exception) {
            Log.e("SoundEffectsManager", "Error playing star chime", e)
        }
    }

    fun playRewardRedeemed() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_SUP_CONFIRM, 300)
        } catch (e: Exception) {
            Log.e("SoundEffectsManager", "Error playing reward chime", e)
        }
    }
}
