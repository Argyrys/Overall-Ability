package com.oaa.livingresume.ui.theme

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.oaa.livingresume.R

/**
 * Bundled typefaces for the OAA institutional-database aesthetic
 * (offline - the TTFs live in `res/font/`, no runtime font provider).
 *
 * - [Rajdhani] - numerals: scores, ranks, grades, percentages, CGPA, plus the
 *   wide-tracked uppercase micro-headers.
 * - [Exo2]     - student details, skill labels, pills and general UI text.
 */

/** Rajdhani SemiBold (600) / Bold (700). */
val Rajdhani = FontFamily(
    Font(R.font.rajdhani_semibold, FontWeight.W600),
    Font(R.font.rajdhani_bold, FontWeight.W700),
)

/** Exo 2 Regular (400) / Medium (500) / SemiBold (600) / Bold (700). */
val Exo2 = FontFamily(
    Font(R.font.exo2_regular, FontWeight.W400),
    Font(R.font.exo2_medium, FontWeight.W500),
    Font(R.font.exo2_semibold, FontWeight.W600),
    Font(R.font.exo2_bold, FontWeight.W700),
)

/**
 * Tabular Rajdhani numerals for every numeric element - scores, ranks,
 * grades, percentages, CGPA. Enables `font-variant-numeric: tabular-nums`
 * (`tnum`) so columns and percentages align cleanly.
 *
 * @param weight 700 (Bold) for scores/grades, 600 (SemiBold) for percentages/ranks.
 */
fun TextStyle.numeric(weight: FontWeight = FontWeight.W700): TextStyle =
    merge(
        SpanStyle(
            fontFamily = Rajdhani,
            fontWeight = weight,
            fontFeatureSettings = "tnum",
        ),
    )

/**
 * Wide-tracked Rajdhani SemiBold micro-header for uppercase HUD labels
 * (`STUDENT OAA CARD`, `OVERALL OAA`, bottom-nav labels...).
 */
fun TextStyle.microHeader(): TextStyle =
    merge(
        SpanStyle(
            fontFamily = Rajdhani,
            fontWeight = FontWeight.W600,
            letterSpacing = 1.6.sp,
        ),
    )

/**
 * Exo 2 body text - 400 for subtitles/details, 500 for skill names and pills.
 */
fun TextStyle.exo(weight: FontWeight): TextStyle =
    merge(SpanStyle(fontFamily = Exo2, fontWeight = weight))
