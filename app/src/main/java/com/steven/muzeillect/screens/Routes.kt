package com.steven.muzeillect.screens

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavController

val LocalNavController = compositionLocalOf<NavController> {
  error("No NavController provided")
}

sealed class Routes(val routeName: String) {
  data object Settings : Routes("settings")
  data object DenyList : Routes("deny_list")
}
