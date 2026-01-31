package com.steven.muzeillect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.*
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.steven.muzeillect.screens.LocalNavBackStack
import com.steven.muzeillect.theme.AppTheme
import com.steven.muzeillect.screens.Routes
import com.steven.muzeillect.screens.blocklist.BlockListScreen
import com.steven.muzeillect.screens.settings.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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

  @Suppress("UNCHECKED_CAST")
  val backStack = rememberNavBackStack(Routes.Settings) as NavBackStack<Routes>

  CompositionLocalProvider(LocalNavBackStack provides backStack) {

    NavDisplay(
      backStack = backStack,
      onBack  = { backStack.removeLastOrNull() },
      entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
      ),
      entryProvider = entryProvider {
        entry<Routes.Settings> { SettingsScreen() }
        entry<Routes.BlockList> { BlockListScreen() }
      }
    )
  }
}
