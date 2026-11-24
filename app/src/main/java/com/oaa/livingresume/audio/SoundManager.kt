package com.oaa.livingresume.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.oaa.livingresume.R

/** The app's short UI sound effects (synthesized WAVs in res/raw). */
enum class Sfx(val resId: Int, val volume: Float) {
    CLICK(R.raw.sfx_click, 0.45f),
    APPROVE(R.raw.sfx_approve, 0.6f),
    REJECT(R.raw.sfx_reject, 0.55f),
    OPEN(R.raw.sfx_open, 0.4f),
    SUBMIT(R.raw.sfx_submit, 0.55f),
}

/**
 * Low-latency SFX player built on [SoundPool].
 * Loads once, plays on demand; silent if loading hasn't finished.
 */
class SoundManager(context: Context) {

    private val appContext = context.applicationContext

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val soundIds = Sfx.entries.associateWith { sfx ->
        soundPool.load(appContext, sfx.resId, 1)
    }

    /** Play a UI sound effect. No-op if called before samples are loaded. */
    fun play(sfx: Sfx) {
        val id = soundIds[sfx] ?: return
        soundPool.play(id, sfx.volume, sfx.volume, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }

    companion object {
        @Volatile
        private var instance: SoundManager? = null

        fun get(context: Context): SoundManager =
            instance ?: synchronized(this) {
                instance ?: SoundManager(context).also { instance = it }
            }
    }
}
