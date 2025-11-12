@file:JvmName("Utils")

package com.steven.muzeillect

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.app.ComponentActivity
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit.*

val Context.settingsDataStore by preferencesDataStore(name = "settings")
val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

val okHttpClient by lazy {
  OkHttpClient.Builder()
    .connectTimeout(30, SECONDS)
    .readTimeout(2, MINUTES)
    .build()
}

const val MUZEI_PACKAGE_NAME = "net.nurik.roman.muzei"

const val BASE_URL = "https://archillect.com/"

const val KEY_TOKEN = "token"

const val MINIMUM_HD_PIXELS = 720 * 1280 * 0.97

fun ComponentActivity.showToast(@StringRes messageResId: Int) {
  lifecycleScope.launch(Dispatchers.Main) {
    Toast.makeText(this@showToast, getText(messageResId), Toast.LENGTH_LONG).show()
  }
}

val Uri.extension: String?
  get() {
    val fileName = lastPathSegment ?: return null
    val split = fileName.splitToSequence(".").asIterable().filter { it.isNotEmpty() }
    if (split.count() < 2) return null
    return split.last()
  }
