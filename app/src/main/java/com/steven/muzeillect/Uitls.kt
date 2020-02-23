@file:JvmName("Utils")

package com.steven.muzeillect

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.muddzdev.styleabletoast.StyleableToast
import okhttp3.OkHttpClient
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.TimeUnit.SECONDS

val okHttpClient: OkHttpClient by lazy {
  OkHttpClient.Builder().connectTimeout(30, SECONDS).readTimeout(2, MINUTES).build()
}

const val MUZEI_PACKAGE_NAME = "net.nurik.roman.muzei"

const val BASE_URL = "http://archillect.com/"

const val KEY_PERMISSION = "permission"

const val EXTENSION_JPG = ".jpg"
const val EXTENSION_JPEG = ".jpeg"
const val EXTENSION_PNG = ".png"

const val HD_TOLERANCE = 0.93

const val MINIMUM_HD_WIDTH = 720 * HD_TOLERANCE
const val MINIMUM_HD_HEIGHT = 1280 * HD_TOLERANCE

private fun buildStyledToast(context: Context, message: String) {
  return StyleableToast.Builder(context)
      .length(Toast.LENGTH_SHORT)
      .text(message)
      .textColor(ContextCompat.getColor(context, R.color.colorPrimary))
      .font(R.font.inconsolata)
      .backgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
      .show()
}

fun showToast(context: Context, message: String) {
  if (Looper.myLooper() == Looper.getMainLooper()) {
    buildStyledToast(context, message)
  } else {
    Handler(Looper.getMainLooper()).post {
      buildStyledToast(context, message)
    }
  }
}

fun Context.isPermissionGranted(permission: String): Boolean {
  return when(ContextCompat.checkSelfPermission(this, permission)){
    PackageManager.PERMISSION_GRANTED -> true
    PackageManager.PERMISSION_DENIED -> false
    else -> throw RuntimeException("unknown permission status")
  }
}
