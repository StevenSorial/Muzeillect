package com.steven.muzeillect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@Composable
fun SettingsColumn(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  val hdKey = booleanPreferencesKey(stringResource(R.string.pref_key_hd))
  val hdEnabled by context.settingsDataStore.data
    .map { preferences -> preferences[hdKey] ?: false }
    .collectAsState(initial = false)

  Column(
    modifier = modifier
      .padding(16.dp)
  ) {
    SwitchPreference(
      title = stringResource(R.string.pref_title_hd),
      checked = hdEnabled,
      onCheckedChange = { checked ->
        scope.launch {
          context.settingsDataStore.edit { preferences ->
            preferences[hdKey] = checked
          }
        }
      }
    )
  }
}

@Composable
private fun SwitchPreference(
  title: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.bodyLarge
    )
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange
    )
  }
}
