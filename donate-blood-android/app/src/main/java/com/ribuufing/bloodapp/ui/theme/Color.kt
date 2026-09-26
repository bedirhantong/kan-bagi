package com.ribuufing.bloodapp.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Base Colors
val PureWhite = Color(0xFFFFFFFF)
val PureBlack = Color(0xFF000000)
val MainColor = Color(0xFFFF9E9E)
val MediumGray = Color(0xFF999999)
val DarkGray = Color(0xFF262626)

val bloodRed = Color(0xFFE53935)
val bloodRedLight = Color(0xFFFFCDD2)
val accentColor = Color(0xFF2196F3)
val accentColorLight = Color(0xFFBBDEFB)
val surfaceColor = Color(0xFFFAFAFA)
val textPrimary = Color(0xFF263238)
val textSecondary = Color(0xFF607D8B)

// Light Theme Colors
internal val LightColors = lightColorScheme(
    primary = PureBlack,
    onPrimary = PureWhite,
    primaryContainer = Color(0xFFF8F8F8),
    onPrimaryContainer = PureBlack,

    secondary = Color(0xFF262626),
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFFF8F8F8),
    onSecondaryContainer = Color(0xFF262626),

    tertiary = Color(0xFF262626),
    onTertiary = PureWhite,
    tertiaryContainer = Color(0xFFF8F8F8),
    onTertiaryContainer = Color(0xFF262626),

    error = Color(0xFFED4956),
    onError = PureWhite,
    errorContainer = Color(0xFFFFEFEB),
    onErrorContainer = Color(0xFFED4956),

    background = PureWhite,
    onBackground = PureBlack,
    surface = PureWhite,
    onSurface = PureBlack,

    surfaceVariant = Color(0xFFF8F8F8),
    onSurfaceVariant = Color(0xFF737373),
    outline = Color(0xFFDBDBDB)
)

// Dark Theme Colors
internal val DarkColors = darkColorScheme(
    primary = PureWhite,
    onPrimary = PureBlack,
    primaryContainer = Color(0xFF121212),
    onPrimaryContainer = PureWhite,

    secondary = Color(0xFFE0E0E0),
    onSecondary = PureBlack,
    secondaryContainer = Color(0xFF121212),
    onSecondaryContainer = Color(0xFFE0E0E0),

    tertiary = Color(0xFFE0E0E0),
    onTertiary = PureBlack,
    tertiaryContainer = Color(0xFF121212),
    onTertiaryContainer = Color(0xFFE0E0E0),

    error = Color(0xFFED4956),
    onError = PureBlack,
    errorContainer = Color(0xFF121212),
    onErrorContainer = Color(0xFFED4956),

    background = PureBlack,
    onBackground = PureWhite,
    surface = PureBlack,
    onSurface = PureWhite,

    surfaceVariant = Color(0xFF121212),
    onSurfaceVariant = Color(0xFF8E8E8E),
    outline = Color(0xFF262626)
)
