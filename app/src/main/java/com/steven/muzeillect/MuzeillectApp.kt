package com.steven.muzeillect

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import timber.log.Timber

class MuzeillectApp : Application() {

  override fun onCreate() {
    super.onCreate()
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree(), FileLoggingTree(this))
  }
}
