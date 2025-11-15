package com.steven.muzeillect.utils

import androidx.compose.ui.Modifier

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
