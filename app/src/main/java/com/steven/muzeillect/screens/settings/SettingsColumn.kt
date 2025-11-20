@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("AssignedValueIsNeverRead")

package com.steven.muzeillect.screens.settings

import com.steven.muzeillect.screens.LocalNavController
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import com.steven.muzeillect.R
import com.steven.muzeillect.screens.Routes
import com.steven.muzeillect.utils.ImageQuality
import com.steven.muzeillect.utils.PrefsKey
import com.steven.muzeillect.utils.nullConditional
import com.steven.muzeillect.utils.settingsDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun SettingsColumn(modifier: Modifier = Modifier) {
  val navController = LocalNavController.current

  Column(modifier = modifier) {
    ImageQualityPreference()
    NavigationTile(
      title = stringResource(R.string.action_blocked_images),
      onClick = { navController.navigate(Routes.BlockList.routeName) },
    )
  }
}

@Composable
fun ImageQualityPreference() {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  val key = PrefsKey.SelectedQuality

  val selected by context.settingsDataStore.data
    .map { prefs -> key.getFrom(prefs) }
    .collectAsState(initial = ImageQuality.ANY)

  var showDialog by remember { mutableStateOf(false) }

  PrefRow(
    title = stringResource(R.string.pref_quality_menu_title),
    subtitle = selected.displayTitle(),
    onClick = { showDialog = true }
  )

  if (showDialog) {
    AlertDialog(
      onDismissRequest = { showDialog = false },
      title = { Text(stringResource(R.string.pref_quality_dialog_title)) },
      text = {
        Column {
          ImageQuality.entries.forEach { quality ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Switch) {
                  showDialog = false
                  scope.launch {
                    context.settingsDataStore.edit {
                      key.setIn(it, quality)
                    }
                  }
                }
                .padding(vertical = 10.dp)
            ) {
              RadioButton(
                selected = quality == selected,
                onClick = null
              )
              Text(
                text = quality.displayTitle(),
                modifier = Modifier.padding(start = 16.dp)
              )
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showDialog = false }) {
          Text(stringResource(android.R.string.cancel))
        }
      }
    )
  }
}

@Suppress("SameParameterValue")
@Composable
private fun NavigationTile(
  title: String,
  onClick: () -> Unit
) {
  PrefRow(
    title = title,
    onClick = onClick
  ) {
    Icon(
      imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null
    )
  }
}

@Composable
private fun PrefRow(
  modifier: Modifier = Modifier,
  title: String,
  subtitle: String? = null,
  onClick: (() -> Unit)? = null,
  trailing: @Composable (RowScope.() -> Unit)? = null,
) {

  val modifier = Modifier
    .fillMaxWidth()
    .nullConditional(onClick, { clickable(onClick = it) })
    .padding(16.dp)
    .then(modifier)

  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {

    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant

      )
      if (subtitle != null) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    if (trailing != null) {
      trailing()
    }
  }
}

@Composable
fun ImageQuality.displayTitle(): String = when (this) {
  ImageQuality.ANY -> stringResource(R.string.pref_quality_any)
  ImageQuality.HD -> stringResource(R.string.pref_quality_hd)
  ImageQuality.FULL_HD -> stringResource(R.string.pref_quality_full_hd)
}
