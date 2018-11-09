package com.steven.muzeillect

import android.app.Application
import timber.log.Timber

class MuzeillectApp : Application() {

  override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree(), FileLoggingTree(this))
  }
}
