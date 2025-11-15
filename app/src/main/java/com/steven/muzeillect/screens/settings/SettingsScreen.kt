package com.steven.muzeillect.screens.settings

import com.steven.muzeillect.uiComponents.AppBar
import com.steven.muzeillect.screens.LocalNavController
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import com.steven.muzeillect.R

@Composable
fun SettingsScreen() {

  Scaffold(
    topBar = { CustomApBar() }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .consumeWindowInsets(padding)
    ) {

      Image(
        painter = painterResource(R.drawable.logo_thick),
        contentDescription = "Logo",
        modifier = Modifier.align(Alignment.CenterHorizontally),
        colorFilter = ColorFilter.tint(colorResource(R.color.onBackground))
      )

      Spacer(modifier = Modifier.weight(0.25f))

      SettingsColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      )
    }
  }
}

@Composable
private fun CustomApBar() {

  val navController = LocalNavController.current
  val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
  val isLaunchedExternally = isLaunchedExternally()

  val canNavigateBack = navController.previousBackStackEntry != null
  AppBar(
    showBackButton = isLaunchedExternally || canNavigateBack,
    onClick = {
      if (canNavigateBack) {
        navController.navigateUp()
      } else {
        backDispatcher?.onBackPressed()
      }
    },
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
