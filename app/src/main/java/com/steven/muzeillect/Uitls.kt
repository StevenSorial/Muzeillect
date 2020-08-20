@file:JvmName("Utils")

package com.steven.muzeillect

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.StringRes
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.TimeUnit.SECONDS

val okHttpClient by lazy {
  OkHttpClient.Builder().connectTimeout(30, SECONDS).readTimeout(2, MINUTES).build()
}

const val MUZEI_PACKAGE_NAME = "net.nurik.roman.muzei"

const val BASE_URL = "https://archillect.com/"

const val KEY_TOKEN = "token"

const val HD_TOLERANCE = 0.93

const val MINIMUM_HD_WIDTH = 720 * HD_TOLERANCE
const val MINIMUM_HD_HEIGHT = 1280 * HD_TOLERANCE

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
    val fileName = lastPathSegment?.trim() ?: return null
    val dotIndex = fileName.lastIndexOf(".")
    if (dotIndex < 0 || dotIndex > fileName.length) return null
    val extension = fileName.substring(dotIndex + 1)
    if (extension.isBlank()) return null
    return extension.toLowerCase()
  }
