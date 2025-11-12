package com.steven.muzeillect

import android.annotation.SuppressLint
import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationManagerCompat
import timber.log.Timber
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.preference.PreferenceManager
import kotlinx.coroutines.launch

class MuzeillectApp : Application() {

  override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    deleteOldChannels()
    migrateSettings()
  }

  @SuppressLint("NewApi")
  private fun deleteOldChannels() {
    NotificationManagerCompat.from(this).apply {
      notificationChannels.forEach {
        deleteNotificationChannel(it.id)
      }
    }
  }

  private fun migrateSettings() {
    val sp = PreferenceManager.getDefaultSharedPreferences(this)
    if (sp.all.isNullOrEmpty()) return
    appScope.launch {
      sp.all.forEach { (key, value) ->
        applicationContext.settingsDataStore.edit { prefs ->
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
//        sp.edit(commit = true) { remove(key) }
      }
    }
  }
}

