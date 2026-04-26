package com.amro.amromovieexplorer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.amro.core.ui.theme.ProvideDimens
import com.amro.core.ui.theme.ProvideSpacing

private val DarkColorScheme = darkColorScheme(
    primary = AMROColorTokens.PrimaryDark,
    secondary = AMROColorTokens.SecondaryDark,
    tertiary = AMROColorTokens.TertiaryDark,
)

private val LightColorScheme = lightColorScheme(
    primary = AMROColorTokens.PrimaryLight,
    secondary = AMROColorTokens.SecondaryLight,
    tertiary = AMROColorTokens.TertiaryLight,

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun AMROMovieExplorerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    ProvideSpacing {
        ProvideDimens {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = Typography,
                content = content,
            )
        }
    }
}