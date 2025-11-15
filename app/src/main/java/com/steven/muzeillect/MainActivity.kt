package com.steven.muzeillect

import com.steven.muzeillect.screens.denylist.DenyListScreen
import com.steven.muzeillect.screens.LocalNavController
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.*
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.steven.muzeillect.theme.AppTheme
import com.steven.muzeillect.screens.Routes
import com.steven.muzeillect.screens.settings.SettingsScreen

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    setContent {
      AppTheme {
        MyNavHost()
      }
    }
  }
}

@Composable
private fun MyNavHost() {
  val navController = rememberNavController()

  CompositionLocalProvider(LocalNavController provides navController) {
    NavHost(
      navController = navController,
      startDestination = Routes.Settings.routeName,
    ) {
      composable(Routes.Settings.routeName) {
        SettingsScreen()
      }
      composable(Routes.DenyList.routeName) {
        DenyListScreen()
      }
    }
  }
}

