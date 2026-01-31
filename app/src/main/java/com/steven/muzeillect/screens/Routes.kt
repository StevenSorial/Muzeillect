package com.steven.muzeillect.screens

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

val LocalNavBackStack = compositionLocalOf<NavBackStack<Routes>> {
  error("No NavBackStack provided")
}

@Serializable
sealed class Routes : NavKey {
  @Serializable
  data object Settings : Routes()

  @Serializable
  data object BlockList : Routes()
}

fun NavBackStack<*>?.hasOneOrMore(): Boolean = (this?.size ?: 0) > 1
