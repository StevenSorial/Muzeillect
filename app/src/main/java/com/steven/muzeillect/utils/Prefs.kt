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
  private val encoder: (value: U) -> T?,
  private val decoder: (value: T?) -> U,
) {
  object DenyList : PrefsKey<Set<String>, Set<String>>(
    rawKey = "pref_blacklist",
    keyGenerator = ::stringSetPreferencesKey,
    encoder = { value -> value },
    decoder = { value -> value ?: emptySet() }
  )

  object SelectedQuality : PrefsKey<String, ImageQuality>(
    rawKey = "selected_quality",
    keyGenerator = ::stringPreferencesKey,
    encoder = { value -> value.prefValue() },
    decoder = { value ->
      ImageQuality.entries.find { it.prefValue() == value }!!
    }
  )

  object Legacy {
    object IsHD : PrefsKey<Boolean, Boolean>(
      rawKey = "pref_hd",
      keyGenerator = ::booleanPreferencesKey,
      encoder = { value -> value },
      decoder = { value -> value ?: false }
    )
  }

  fun asDSKey(): Preferences.Key<T> = keyGenerator(rawKey)

  fun getFrom(prefs: Preferences): U {
    val found = prefs[asDSKey()]
    return decoder(found)
  }

  fun setIn(prefs: MutablePreferences, value: U?) {
    val key = asDSKey()
    val rawValue = value?.let { this.encoder(it) }
    if (rawValue == null) {
      prefs.remove(key)
      return
    }
    prefs[key] = rawValue
  }
}

private fun ImageQuality.prefValue(): String? = when (this) {
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
