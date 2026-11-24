package com.oaa.livingresume.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.oaa.livingresume.data.model.Grade
import com.oaa.livingresume.data.model.Pillar

// ---- Futuristic anime-HUD palette -----------------------------------------
// The colour names below are stable across the whole app; each one resolves
// through [OaaThemeState] so the manual dark/light toggle repaints everything
// without a single call site changing.

private data class OaaPalette(
    val background: Color,
    val surface: Color,
    val card: Color,
    val cardAlt: Color,
    val cardDeep: Color,
    val primary: Color,
    val gold: Color,
    val indigo: Color,
    val green: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val outline: Color,
    val danger: Color,
    val amber: Color,
    val academic: Color,
    val adaptability: Color,
    val social: Color,
    val physical: Color,
)

private val DarkPalette = OaaPalette(
    background = Color(0xFF090D16),    // DeepNavy - app background
    surface = Color(0xFF0D1422),       // NavySurface - bottom bar / sheets
    card = Color(0xFF111A2C),          // NavyCard
    cardAlt = Color(0xFF0C1526),       // hero card gradient top
    cardDeep = Color(0xFF0A1020),      // hero card gradient bottom
    primary = Color(0xFF00E5FF),       // CyanGlow
    gold = Color(0xFFFFD54F),          // GoldGlow
    indigo = Color(0xFF6C63FF),        // IndigoGlow
    green = Color(0xFF3DDC97),         // GreenGlow
    textPrimary = Color(0xFFE6EEF9),
    textSecondary = Color(0xFF8D9AB0),
    outline = Color(0xFF1F2B45),
    danger = Color(0xFFFF5C7A),
    amber = Color(0xFFFFB74D),
    academic = Color(0xFF00E5FF),
    adaptability = Color(0xFF6C63FF),
    social = Color(0xFFFFD54F),
    physical = Color(0xFF3DDC97),
)

private val LightPalette = OaaPalette(
    background = Color(0xFFF2F5FC),    // soft paper-navy
    surface = Color(0xFFFFFFFF),       // bottom bar / sheets
    card = Color(0xFFFFFFFF),
    cardAlt = Color(0xFFFFFFFF),
    cardDeep = Color(0xFFE9EEF9),      // hero card gradient bottom
    primary = Color(0xFF007C91),       // readable cyan ink
    gold = Color(0xFFC08A00),          // readable gold ink
    indigo = Color(0xFF5B52E0),
    green = Color(0xFF12996E),
    textPrimary = Color(0xFF0D1422),
    textSecondary = Color(0xFF5B6880),
    outline = Color(0xFFD5DCEC),
    danger = Color(0xFFD93A5A),
    amber = Color(0xFFE08A2E),
    academic = Color(0xFF007C91),
    adaptability = Color(0xFF5B52E0),
    social = Color(0xFFB07D00),
    physical = Color(0xFF12996E),
)

/**
 * Manual theme switch (sun/moon on Home). Dark is the default; the choice is
 * persisted to SharedPreferences by [load] / [persist].
 */
object OaaThemeState {

    /** True when the light palette is active. Read during composition to repaint. */
    internal var isLight by mutableStateOf(false)
        private set

    fun toggle() {
        isLight = !isLight
    }

    fun load(context: Context) {
        isLight = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_LIGHT, false)
    }

    fun persist(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_LIGHT, isLight)
            .apply()
    }

    private const val PREFS = "oaa_theme"
    private const val KEY_LIGHT = "light"
}

private val palette: OaaPalette
    get() = if (OaaThemeState.isLight) LightPalette else DarkPalette

// ---- Dynamic colour names (same names everywhere in the app) ---------------
val DeepNavy: Color get() = palette.background
val NavySurface: Color get() = palette.surface
val NavyCard: Color get() = palette.card
val NavyCardAlt: Color get() = palette.cardAlt
val NavyCardDeep: Color get() = palette.cardDeep
val CyanGlow: Color get() = palette.primary
val GoldGlow: Color get() = palette.gold
val IndigoGlow: Color get() = palette.indigo
val GreenGlow: Color get() = palette.green
val TextPrimary: Color get() = palette.textPrimary
val TextSecondary: Color get() = palette.textSecondary
val OutlineFaint: Color get() = palette.outline
val DangerRed: Color get() = palette.danger
val AmberSoft: Color get() = palette.amber

/** Fixed dark ink for labels/icons sitting ON a cyan or gold fill (both themes). */
val OnAccent = Color(0xFF090D16)

/** Per-pillar accent colour (theme-aware so it stays legible on paper). */
val Pillar.accent: Color
    get() = if (OaaThemeState.isLight) {
        when (this) {
            Pillar.ACADEMIC -> LightPalette.academic
            Pillar.ADAPTABILITY -> LightPalette.adaptability
            Pillar.SOCIAL -> LightPalette.social
            Pillar.PHYSICAL -> LightPalette.physical
        }
    } else {
        Color(colorValue)
    }

/** Letter-grade colour: gold for A tiers, cyan for B tiers, warm/red below. */
fun gradeColor(grade: Grade): Color = when (grade) {
    Grade.A_PLUS, Grade.A -> GoldGlow
    Grade.B_PLUS, Grade.B -> CyanGlow
    Grade.C -> AmberSoft
    Grade.D -> if (OaaThemeState.isLight) Color(0xFFD96A2C) else Color(0xFFFF8A65)
    Grade.E -> DangerRed
}

/** Pillar tint used for the timeline / status chips. */
fun semesterStatusColor(status: com.oaa.livingresume.data.model.SemesterStatus): Color =
    when (status) {
        com.oaa.livingresume.data.model.SemesterStatus.COMPLETED -> CyanGlow
        com.oaa.livingresume.data.model.SemesterStatus.ONGOING -> GoldGlow
        com.oaa.livingresume.data.model.SemesterStatus.UPCOMING -> TextSecondary
    }
