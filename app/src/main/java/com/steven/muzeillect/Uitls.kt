@file:JvmName("Utils")

package com.steven.muzeillect

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.StringRes
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit.*

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

fun Context.showToast(@StringRes messageResId: Int, duration: Int) {
  if (Looper.myLooper() == Looper.getMainLooper()) {
    Toast.makeText(this, getString(messageResId), duration).show()
  } else {
    Handler(Looper.getMainLooper()).post {
      Toast.makeText(this, getString(messageResId), duration).show()
    }
  }
}

val Uri.extension: String?
  get() {
    val fileName = lastPathSegment ?: return null
    val split = fileName.splitToSequence(".").asIterable().filter { it.isNotEmpty() }
    if (split.count() < 2) return null
    return split.last()
  }
