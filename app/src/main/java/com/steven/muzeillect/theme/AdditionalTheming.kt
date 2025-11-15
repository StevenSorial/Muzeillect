package com.steven.muzeillect.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Suppress("DEPRECATION")
@Composable
fun SetStatusBarColors(scheme: ColorScheme) {
  val view = LocalView.current

  if (view.isInEditMode) return

  SideEffect {
    val isLight = scheme.background.luminance() > 0.5f

    val window = (view.context as Activity).window

    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLight
    window.statusBarColor = scheme.background.toArgb()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      window.navigationBarColor = scheme.background.toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = isLight
    } else {
      window.navigationBarColor = scheme.primary.toArgb()
    }
  }
}
