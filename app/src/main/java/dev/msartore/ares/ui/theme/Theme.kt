package dev.msartore.ares.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dev.msartore.ares.models.Rgb
import dev.msartore.ares.ui.theme.Theme.background
import dev.msartore.ares.ui.theme.Theme.container
import dev.msartore.ares.ui.theme.Theme.darkTheme
import dev.msartore.ares.utils.cor
import dev.msartore.ares.viewmodels.MainViewModel

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)


private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)

object Theme {
    var darkTheme = false
    var container = Rgb()
    var background = Rgb()
}

@Composable
fun AresTheme(
    mainViewModel: MainViewModel,
    changeStatusBarColor: MutableState<() -> Unit>,
    isNavBarColorSet: MutableState<Boolean>,
    content: @Composable () -> Unit
) {
    darkTheme = isSystemInDarkTheme()
    val systemUiController = rememberSystemUiController()
    val colorScheme = when {
        mainViewModel.settings?.isMaterialYouEnabled?.value == true && darkTheme ->
            dynamicDarkColorScheme(LocalContext.current)
        mainViewModel.settings?.isMaterialYouEnabled?.value == true && !darkTheme ->
            dynamicLightColorScheme(LocalContext.current)
        darkTheme -> DarkColors
        else -> LightColors
    }
    val surfaceColor = colorScheme.surfaceColorAtElevation(3.dp)

    container.run {
        g = surfaceColor.green * 255
        b = surfaceColor.blue * 255
        r = surfaceColor.red * 255
    }

    background.run {
        g = colorScheme.background.green * 255
        b = colorScheme.background.blue * 255
        r = colorScheme.background.red * 255
    }

    changeStatusBarColor.value = {
        systemUiController.setSystemBarsColor(
            color = colorScheme.background,
            darkIcons = !darkTheme
        )
    }

    cor { mainViewModel.isDarkTheme.emit(darkTheme) }

    systemUiController.setSystemBarsColor(
        color = colorScheme.background,
        darkIcons = !darkTheme
    )

    if (isNavBarColorSet.value)
        systemUiController.setNavigationBarColor(
            color = colorScheme.surfaceColorAtElevation(3.dp),
            darkIcons = !darkTheme
        )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}