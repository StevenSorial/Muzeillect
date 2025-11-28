package com.steven.muzeillect.utils

import android.content.Context
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.PreferenceManager
import kotlin.collections.component1
import kotlin.collections.component2

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

sealed class PrefsKey<T : Any, U>(
  private val rawKey: String,
  private val keyGenerator: (name: String) -> Preferences.Key<T>,
  private val defaultValue: U,
  private val encoder: (parsedValue: U) -> T?,
  private val decoder: (rawValue: T) -> U,
) {
  object BlockList : PrefsKey<Set<String>, Set<String>>(
    rawKey = "pref_blacklist",
    keyGenerator = ::stringSetPreferencesKey,
    defaultValue = emptySet(),
    encoder = { parsedValue -> parsedValue },
    decoder = { rawValue -> rawValue }
  )

  object SelectedQuality : PrefsKey<String, ImageQuality>(
    rawKey = "selected_quality",
    defaultValue = ImageQuality.ANY,
    keyGenerator = ::stringPreferencesKey,
    encoder = { parsedValue -> parsedValue.prefValue },
    decoder = { rawValue ->
      ImageQuality.entries.find { it.prefValue == rawValue }!!
    }
  )

  object Legacy {
    object IsHD : PrefsKey<Boolean, Boolean>(
      rawKey = "pref_hd",
      keyGenerator = ::booleanPreferencesKey,
      defaultValue = false,
      encoder = { parsedValue -> parsedValue },
      decoder = { rawValue -> rawValue }
    )
  }

  fun asDSKey(): Preferences.Key<T> = keyGenerator(rawKey)

  fun getFrom(prefs: Preferences): U {
    try {
      val rawValue = prefs[asDSKey()]
      return rawValue?.let { decoder(it) } ?: defaultValue
    } catch (_: Exception) {
      return defaultValue
    }
  }

  fun setIn(prefs: MutablePreferences, value: U?) {
    val key = asDSKey()
    val rawValue = value?.let { this.encoder(it) }
    if (rawValue != null) {
      prefs[key] = rawValue
    } else {
      prefs.remove(key)
    }
  }
}

private val ImageQuality.prefValue: String?
  get() = when (this) {
    ImageQuality.ANY -> null
    ImageQuality.HD -> "hd"
    ImageQuality.FULL_HD -> "full_hd"
  }

object PreferenceMigrator {
  suspend fun migrateFromSharedPreferences(context: Context) {
    val sp = PreferenceManager.getDefaultSharedPreferences(context)
    if (sp.all.isNullOrEmpty()) return
    sp.all.forEach { (key, value) ->
      context.settingsDataStore.edit { prefs ->
        when (value) {
          is Boolean -> prefs[booleanPreferencesKey(key)] = value
          is Int -> prefs[intPreferencesKey(key)] = value
          is Long -> prefs[longPreferencesKey(key)] = value
          is Float -> prefs[floatPreferencesKey(key)] = value
          is String -> prefs[stringPreferencesKey(key)] = value
          is Set<*> -> {
            val set = sp.getStringSet(key, emptySet()) ?: emptySet()
            prefs[stringSetPreferencesKey(key)] = set.toSet()
          }
        }
      }
      sp.edit(commit = true) { remove(key) }
    }
  }

  suspend fun migrateKeyStoreKeys(context: Context) {
    context.settingsDataStore.edit {
      migrateImageQuality(it)
    }
  }

  private fun migrateImageQuality(prefs: MutablePreferences) {
    val oldKey = PrefsKey.Legacy.IsHD
    if (!prefs.contains(oldKey.asDSKey())) return
    val isHD = oldKey.getFrom(prefs)
    val newQuality = if (isHD) ImageQuality.HD else ImageQuality.ANY
    PrefsKey.SelectedQuality.setIn(prefs, newQuality)
    prefs.remove(oldKey.asDSKey())
  }
}
