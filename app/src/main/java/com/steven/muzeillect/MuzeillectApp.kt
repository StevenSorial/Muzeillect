package com.steven.muzeillect

import android.annotation.SuppressLint
import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationManagerCompat
import timber.log.Timber
import com.steven.muzeillect.utils.PreferenceMigrator
import com.steven.muzeillect.utils.appScope
import kotlinx.coroutines.launch

class MuzeillectApp : Application() {

  override fun onCreate() {
    super.onCreate()
    NetworkClient.initClients(this)
    if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    deleteOldChannels()
    appScope.launch {
      PreferenceMigrator.migrateFromSharedPreferences(applicationContext)
      PreferenceMigrator.migrateKeyStoreKeys(applicationContext)
    }
  }

  @SuppressLint("NewApi")
  private fun deleteOldChannels() {
    NotificationManagerCompat.from(this).apply {
      notificationChannels.forEach {
        deleteNotificationChannel(it.id)
      }
    }
  }
}
