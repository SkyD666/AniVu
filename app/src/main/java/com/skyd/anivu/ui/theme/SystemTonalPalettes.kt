package com.skyd.anivu.ui.theme

import android.content.Context
import android.os.Build
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.Contrast
import com.materialkolor.dynamiccolor.MaterialDynamicColors
import com.materialkolor.ktx.from
import com.materialkolor.ktx.getColor
import com.materialkolor.ktx.toHct
import com.materialkolor.palettes.TonalPalette
import com.materialkolor.scheme.DynamicScheme
import com.materialkolor.scheme.Variant

private fun getColorFromTheme(context: Context, @ColorRes id: Int): Color {
    return Color(context.resources.getColor(id, context.theme))
}

@RequiresApi(Build.VERSION_CODES.S)
fun primarySystem(context: Context, tone: Int = 50): Color = when (tone) {
    0 -> getColorFromTheme(context, android.R.color.system_accent1_1000)
    10 -> getColorFromTheme(context, android.R.color.system_accent1_900)
    20 -> getColorFromTheme(context, android.R.color.system_accent1_800)
    30 -> getColorFromTheme(context, android.R.color.system_accent1_700)
    40 -> getColorFromTheme(context, android.R.color.system_accent1_600)
    50 -> getColorFromTheme(context, android.R.color.system_accent1_500)
    60 -> getColorFromTheme(context, android.R.color.system_accent1_400)
    70 -> getColorFromTheme(context, android.R.color.system_accent1_300)
    80 -> getColorFromTheme(context, android.R.color.system_accent1_200)
    90 -> getColorFromTheme(context, android.R.color.system_accent1_100)
    95 -> getColorFromTheme(context, android.R.color.system_accent1_50)
    99 -> getColorFromTheme(context, android.R.color.system_accent1_10)
    100 -> getColorFromTheme(context, android.R.color.system_accent1_0)
    else -> throw IllegalArgumentException("Unknown primary tone: $tone")
}

@RequiresApi(Build.VERSION_CODES.S)
private fun secondarySystem(context: Context, tone: Int = 50): Color = when (tone) {
    0 -> getColorFromTheme(context, android.R.color.system_accent2_1000)
    10 -> getColorFromTheme(context, android.R.color.system_accent2_900)
    20 -> getColorFromTheme(context, android.R.color.system_accent2_800)
    30 -> getColorFromTheme(context, android.R.color.system_accent2_700)
    40 -> getColorFromTheme(context, android.R.color.system_accent2_600)
    50 -> getColorFromTheme(context, android.R.color.system_accent2_500)
    60 -> getColorFromTheme(context, android.R.color.system_accent2_400)
    70 -> getColorFromTheme(context, android.R.color.system_accent2_300)
    80 -> getColorFromTheme(context, android.R.color.system_accent2_200)
    90 -> getColorFromTheme(context, android.R.color.system_accent2_100)
    95 -> getColorFromTheme(context, android.R.color.system_accent2_50)
    99 -> getColorFromTheme(context, android.R.color.system_accent2_10)
    100 -> getColorFromTheme(context, android.R.color.system_accent2_0)
    else -> throw IllegalArgumentException("Unknown secondary tone: $tone")
}

@RequiresApi(Build.VERSION_CODES.S)
private fun tertiarySystem(context: Context, tone: Int = 50): Color = when (tone) {
    0 -> getColorFromTheme(context, android.R.color.system_accent3_1000)
    10 -> getColorFromTheme(context, android.R.color.system_accent3_900)
    20 -> getColorFromTheme(context, android.R.color.system_accent3_800)
    30 -> getColorFromTheme(context, android.R.color.system_accent3_700)
    40 -> getColorFromTheme(context, android.R.color.system_accent3_600)
    50 -> getColorFromTheme(context, android.R.color.system_accent3_500)
    60 -> getColorFromTheme(context, android.R.color.system_accent3_400)
    70 -> getColorFromTheme(context, android.R.color.system_accent3_300)
    80 -> getColorFromTheme(context, android.R.color.system_accent3_200)
    90 -> getColorFromTheme(context, android.R.color.system_accent3_100)
    95 -> getColorFromTheme(context, android.R.color.system_accent3_50)
    99 -> getColorFromTheme(context, android.R.color.system_accent3_10)
    100 -> getColorFromTheme(context, android.R.color.system_accent3_0)
    else -> throw IllegalArgumentException("Unknown tertiary tone: $tone")
}

@RequiresApi(Build.VERSION_CODES.S)
private fun neutralSystem(context: Context, tone: Int = 50): Color = when (tone) {
    0 -> getColorFromTheme(context, android.R.color.system_neutral1_1000)
    10 -> getColorFromTheme(context, android.R.color.system_neutral1_900)
    20 -> getColorFromTheme(context, android.R.color.system_neutral1_800)
    30 -> getColorFromTheme(context, android.R.color.system_neutral1_700)
    40 -> getColorFromTheme(context, android.R.color.system_neutral1_600)
    50 -> getColorFromTheme(context, android.R.color.system_neutral1_500)
    60 -> getColorFromTheme(context, android.R.color.system_neutral1_400)
    70 -> getColorFromTheme(context, android.R.color.system_neutral1_300)
    80 -> getColorFromTheme(context, android.R.color.system_neutral1_200)
    90 -> getColorFromTheme(context, android.R.color.system_neutral1_100)
    95 -> getColorFromTheme(context, android.R.color.system_neutral1_50)
    99 -> getColorFromTheme(context, android.R.color.system_neutral1_10)
    100 -> getColorFromTheme(context, android.R.color.system_neutral1_0)
    else -> throw IllegalArgumentException("Unknown neutral tone: $tone")
}

@RequiresApi(Build.VERSION_CODES.S)
private fun neutralVariantSystem(context: Context, tone: Int = 50): Color = when (tone) {
    0 -> getColorFromTheme(context, android.R.color.system_neutral2_1000)
    10 -> getColorFromTheme(context, android.R.color.system_neutral2_900)
    20 -> getColorFromTheme(context, android.R.color.system_neutral2_800)
    30 -> getColorFromTheme(context, android.R.color.system_neutral2_700)
    40 -> getColorFromTheme(context, android.R.color.system_neutral2_600)
    50 -> getColorFromTheme(context, android.R.color.system_neutral2_500)
    60 -> getColorFromTheme(context, android.R.color.system_neutral2_400)
    70 -> getColorFromTheme(context, android.R.color.system_neutral2_300)
    80 -> getColorFromTheme(context, android.R.color.system_neutral2_200)
    90 -> getColorFromTheme(context, android.R.color.system_neutral2_100)
    95 -> getColorFromTheme(context, android.R.color.system_neutral2_50)
    99 -> getColorFromTheme(context, android.R.color.system_neutral2_10)
    100 -> getColorFromTheme(context, android.R.color.system_neutral2_0)
    else -> throw IllegalArgumentException("Unknown neutral variant tone: $tone")
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun rememberSystemDynamicColorScheme(
    isDark: Boolean,
    variant: Variant = Variant.TONAL_SPOT,
    contrastLevel: Double = 0.0,
    isExtendedFidelity: Boolean = false,
): ColorScheme {
    val context = LocalContext.current
    return remember(context, isDark, variant, contrastLevel, isExtendedFidelity) {
        systemDynamicColorScheme(context, isDark, variant, contrastLevel, isExtendedFidelity)
    }
}

@RequiresApi(Build.VERSION_CODES.S)
fun systemDynamicColorScheme(
    context: Context,
    isDark: Boolean,
    variant: Variant = Variant.TONAL_SPOT,
    contrastLevel: Double = Contrast.Default.value,
    isExtendedFidelity: Boolean = false,
): ColorScheme {
    val primaryColor = primarySystem(context)
    val scheme = DynamicScheme(
        sourceColorHct = primaryColor.toHct(),
        variant = variant,
        isDark = isDark,
        contrastLevel = contrastLevel,
        primaryPalette = TonalPalette.from(primaryColor),
        secondaryPalette = TonalPalette.from(secondarySystem(context)),
        tertiaryPalette = TonalPalette.from(tertiarySystem(context)),
        neutralPalette = TonalPalette.from(neutralSystem(context)),
        neutralVariantPalette = TonalPalette.from(neutralVariantSystem(context)),
    )
    val colors = MaterialDynamicColors(isExtendedFidelity)

    return ColorScheme(
        background = colors.background().getColor(scheme),
        error = colors.error().getColor(scheme),
        errorContainer = colors.errorContainer().getColor(scheme),
        inverseOnSurface = colors.inverseOnSurface().getColor(scheme),
        inversePrimary = colors.inversePrimary().getColor(scheme),
        inverseSurface = colors.inverseSurface().getColor(scheme),
        onBackground = colors.onBackground().getColor(scheme),
        onError = colors.onError().getColor(scheme),
        onErrorContainer = colors.onErrorContainer().getColor(scheme),
        onPrimary = colors.onPrimary().getColor(scheme),
        onPrimaryContainer = colors.onPrimaryContainer().getColor(scheme),
        onSecondary = colors.onSecondary().getColor(scheme),
        onSecondaryContainer = colors.onSecondaryContainer().getColor(scheme),
        onSurface = colors.onSurface().getColor(scheme),
        onSurfaceVariant = colors.onSurfaceVariant().getColor(scheme),
        onTertiary = colors.onTertiary().getColor(scheme),
        onTertiaryContainer = colors.onTertiaryContainer().getColor(scheme),
        outline = colors.outline().getColor(scheme),
        outlineVariant = colors.outlineVariant().getColor(scheme),
        primary = colors.primary().getColor(scheme),
        primaryContainer = colors.primaryContainer().getColor(scheme),
        scrim = colors.scrim().getColor(scheme),
        secondary = colors.secondary().getColor(scheme),
        secondaryContainer = colors.secondaryContainer().getColor(scheme),
        surface = colors.surface().getColor(scheme),
        surfaceTint = colors.surfaceTint().getColor(scheme),
        surfaceBright = colors.surfaceBright().getColor(scheme),
        surfaceDim = colors.surfaceDim().getColor(scheme),
        surfaceContainer = colors.surfaceContainer().getColor(scheme),
        surfaceContainerHigh = colors.surfaceContainerHigh().getColor(scheme),
        surfaceContainerHighest = colors.surfaceContainerHighest().getColor(scheme),
        surfaceContainerLow = colors.surfaceContainerLow().getColor(scheme),
        surfaceContainerLowest = colors.surfaceContainerLowest().getColor(scheme),
        surfaceVariant = colors.surfaceVariant().getColor(scheme),
        tertiary = colors.tertiary().getColor(scheme),
        tertiaryContainer = colors.tertiaryContainer().getColor(scheme),
    )
}
