package com.steven.muzeillect

import android.os.Bundle
import androidx.compose.foundation.layout.padding
import androidx.activity.compose.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController

class MainActivity : FragmentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
      ) {
        AppScaffold()
      }
    }
  }
}

@Composable
fun AppScaffold() {
  val navController = rememberNavController()

  Scaffold(
    topBar = { AppBar(navController) }
  ) { padding ->
    NavHost(
      navController = navController,
      startDestination = "settings_container",
      modifier = Modifier.padding(padding)
    ) {
      composable("settings_container") {
        SettingsContainerScreen()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(navController: NavController) {
  val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
  val isLaunchedExternally = isLaunchedExternally()

  val canNavigateBack = navController.previousBackStackEntry != null

  TopAppBar(
    title = { },
    navigationIcon = {
      if (isLaunchedExternally || canNavigateBack) {
        IconButton(
          onClick = {
            if (canNavigateBack) {
              navController.navigateUp()
            } else {
              backDispatcher?.onBackPressed()
            }
          }
        ) {
          Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.background,
      titleContentColor = MaterialTheme.colorScheme.onBackground
    )
  )
}

@Composable
private fun isLaunchedExternally(): Boolean {
  val activity = LocalActivity.current
  val intent = activity?.intent
  val callingPkg = activity?.callingActivity?.packageName
  val targetPkg = intent?.`package`

  return remember {
    if (callingPkg != null && callingPkg != activity.packageName) {
      true
    } else if (targetPkg != null && targetPkg != activity.packageName) {
      true
    } else {
      false
    }
  }
}
