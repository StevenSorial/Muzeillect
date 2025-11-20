package com.steven.muzeillect.uiComponents

import com.steven.muzeillect.screens.LocalNavController
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
import androidx.compose.ui.res.stringResource
import com.steven.muzeillect.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppBar(
  showBackButton: Boolean? = null,
  title: @Composable (() -> Unit) = {},
  onClick: (() -> Unit)? = null
) {

  val navController = LocalNavController.current
  val canNavigateBack = navController.previousBackStackEntry != null

  TopAppBar(
    title = title,
    navigationIcon = {
      if (showBackButton ?: canNavigateBack) {
        val description = stringResource(R.string.back)
        TooltipBox(
          positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Below
          ),
          state = rememberTooltipState(),
          tooltip = { PlainTooltip { Text(description) } },
        ) {
          IconButton(
            onClick = {
              if (onClick != null) {
                onClick()
              } else {
                navController.navigateUp()
              }
            }
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = description
            )
          }
        }
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.background,
      titleContentColor = MaterialTheme.colorScheme.onBackground
    )
  )
}
