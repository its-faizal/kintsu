package com.kintsu.app.audio

import androidx.annotation.RawRes
import com.kintsu.app.R

enum class ZenSoundTrack(
    val id: String,
    val title: String,
    val description: String,
    @RawRes val rawResId: Int
) {
    BAMBOO_RAIN(
        id = "rain",
        title = "Bamboo Forest Rain",
        description = "Gentle rainfall on bamboo grove",
        rawResId = R.raw.zen_sound_rain
    ),
    SINGING_BOWL(
        id = "bowl",
        title = "Sacred Singing Bowl",
        description = "Resonant 432Hz bronze meditation harmonics",
        rawResId = R.raw.zen_sound_bowl
    ),
    RIVER_KOTO(
        id = "stream",
        title = "River Stream & Koto",
        description = "Mountain stream water with ambient strings",
        rawResId = R.raw.zen_sound_stream
    ),
    FIRESIDE_EMBERS(
        id = "fire",
        title = "Fireside Embers",
        description = "Warm crackling cedar hearth fire",
        rawResId = R.raw.zen_sound_fire
    ),
    NIGHT_MIST_BELL(
        id = "bell",
        title = "Night Mist & Temple Bell",
        description = "Atmospheric night breeze with distant bronze bell",
        rawResId = R.raw.zen_sound_bell
    );

    companion object {
        fun fromId(id: String): ZenSoundTrack? = values().find { it.id == id }
    }
}
