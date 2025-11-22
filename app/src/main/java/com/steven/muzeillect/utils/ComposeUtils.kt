package com.steven.muzeillect.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp

@Suppress("unused")
inline fun Modifier.conditional(
  condition: Boolean,
  ifTrue: Modifier.() -> Modifier,
  ifFalse: Modifier.() -> Modifier = { this },
): Modifier {
  val newModifier = if (condition) {
    ifTrue()
  } else {
    ifFalse()
  }
  return then(newModifier)

}

@Composable
fun <T : PaddingValues> T.copyWith(
  start: Dp = calculateStartPadding(LocalLayoutDirection.current),
  top: Dp = calculateTopPadding(),
  end: Dp = calculateEndPadding(LocalLayoutDirection.current),
  bottom: Dp = calculateBottomPadding(),
): PaddingValues {

  return PaddingValues(
    start = start,
    top = top,
    end = end,
    bottom = bottom
  )
}

inline fun <T : Any> Modifier.nullConditional(
  value: T?,
  ifNotNull: Modifier.(T) -> Modifier = { this },
  ifNull: Modifier.() -> Modifier = { this },
): Modifier {

  val newModifier = if (value != null) {
    ifNotNull(value)
  } else {
    ifNull()
  }

  return then(newModifier)
}
