package com.steven.muzeillect

import android.annotation.SuppressLint
import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationManagerCompat
import timber.log.Timber

class MuzeillectApp : Application() {

  override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    deleteOldChannels()
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
