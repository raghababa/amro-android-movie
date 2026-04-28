package com.amro.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Radii(
    val sm: Dp = 8.dp,
)

data class ComponentSizes(
    val moviePosterWidth: Dp = 72.dp,
    val moviePosterHeight: Dp = 108.dp,
    val detailPosterHeight: Dp = 300.dp,
    val heroPosterWidth: Dp = 240.dp,
    val heroPosterHeight: Dp = 240.dp,
)

data class Elevations(
    val subtle: Dp = 1.dp,
    val card: Dp = 8.dp,
)

data class Dimens(
    val radii: Radii = Radii(),
    val sizes: ComponentSizes = ComponentSizes(),
    val elevations: Elevations = Elevations(),
)

val LocalDimens = staticCompositionLocalOf { Dimens() }

val MaterialTheme.dimens: Dimens
    @Composable
    get() = LocalDimens.current

@Composable
fun ProvideDimens(
    dimens: Dimens = Dimens(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalDimens provides dimens,
        content = content,
    )
}

