package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = AmberWarm,
  onPrimary = EspressoBlack,
  primaryContainer = CoffeeBrownMedium,
  onPrimaryContainer = CoffeeCream,
  secondary = CoffeeBrownLight,
  onSecondary = Color.White,
  background = EspressoBlack,
  surface = CoffeeBrownDark,
  onBackground = CoffeeCream,
  onSurface = CoffeeCream
)

private val LightColorScheme = lightColorScheme(
  primary = CoffeeBrownDark,
  onPrimary = Color.White,
  primaryContainer = CoffeeCreamDarker,
  onPrimaryContainer = CoffeeBrownDark,
  secondary = CoffeeBrownMedium,
  onSecondary = Color.White,
  tertiary = AmberWarm,
  background = CoffeeCream,
  surface = Color.White,
  onBackground = CoffeeBrownDark,
  onSurface = CoffeeBrownDark,
  surfaceVariant = CoffeeCreamDarker,
  onSurfaceVariant = CoffeeBrownMedium
)

@Composable
fun CozyCafeTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use our handcrafted cozy cafe theme
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
