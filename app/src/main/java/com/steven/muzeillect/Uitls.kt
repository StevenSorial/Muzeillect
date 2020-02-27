@file:JvmName("Utils")

package com.steven.muzeillect

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.muddzdev.styleabletoast.StyleableToast
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.TimeUnit.SECONDS


val okHttpClient: OkHttpClient by lazy {
  OkHttpClient.Builder().connectTimeout(30, SECONDS).readTimeout(2, MINUTES).build()
}

const val MUZEI_PACKAGE_NAME = "net.nurik.roman.muzei"

const val BASE_URL = "https://archillect.com/"

const val KEY_PERMISSION = "permission"

const val HD_TOLERANCE = 0.93

const val MINIMUM_HD_WIDTH = 720 * HD_TOLERANCE
const val MINIMUM_HD_HEIGHT = 1280 * HD_TOLERANCE

fun Context.showToast(message: String) {
  if (Looper.myLooper() == Looper.getMainLooper()) {
    buildStyledToast(this, message)
  } else {
    Handler(Looper.getMainLooper()).post {
      buildStyledToast(this, message)
    }
  }
}

fun NotificationManagerCompat.isNotificationChannelEnabled(channelId: String): Boolean {
  if (channelId.isBlank()) throw RuntimeException("Notification id is empty")
  val isEnabled = areNotificationsEnabled()
  if (!isEnabled) return false
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    val channel = getNotificationChannel(channelId)
    return channel!!.importance != NotificationManager.IMPORTANCE_NONE
  } else {
    return isEnabled
  }
}

private fun buildStyledToast(context: Context, message: String) {
  return StyleableToast.Builder(context)
      .length(Toast.LENGTH_SHORT)
      .text(message)
      .textColor(ContextCompat.getColor(context, R.color.colorPrimary))
      .font(R.font.inconsolata)
      .backgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
      .show()
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

fun Context.isPermissionGranted(permission: String): Boolean {
  return when (ContextCompat.checkSelfPermission(this, permission)) {
    PackageManager.PERMISSION_GRANTED -> true
    PackageManager.PERMISSION_DENIED -> false
    else -> throw RuntimeException("unknown permission status")
  }
}
