package com.steven.muzeillect.uiComponents

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.steven.muzeillect.R
import com.steven.muzeillect.screens.LocalNavBackStack
import com.steven.muzeillect.screens.hasOneOrMore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppBar(
  modifier: Modifier = Modifier,
  showBackButton: Boolean? = null,
  title: @Composable (() -> Unit) = {},
  onClick: (() -> Unit)? = null
) {
  val backStack = LocalNavBackStack.current
  val showBackButton = showBackButton ?: backStack.hasOneOrMore()
  val onClick: () -> Unit = onClick ?: { backStack.removeLastOrNull() }

  TopAppBar(
    title = title,
    modifier = modifier,
    navigationIcon = {
      if (!showBackButton) return@TopAppBar

      val description = stringResource(R.string.back)
      val hapticFeedback = LocalHapticFeedback.current
      val tooltipState = rememberTooltipState()

      LaunchedEffect(tooltipState.isVisible) {
        if (!tooltipState.isVisible) return@LaunchedEffect
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
      }

      TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
          positioning = TooltipAnchorPosition.Below
        ),
        state = tooltipState,
        tooltip = { PlainTooltip { Text(description) } },
      ) {
        IconButton(onClick = onClick) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = description
          )
        }
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.background,
      titleContentColor = MaterialTheme.colorScheme.onBackground
    )
  )
}
