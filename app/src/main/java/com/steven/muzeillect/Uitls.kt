@file:JvmName("Utils")

package com.steven.muzeillect

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.muddzdev.styleabletoast.StyleableToast
import java.util.Random
import java.util.concurrent.ThreadLocalRandom

const val RETRY_INTERVAL = 5L

const val MUZEI_PACKAGE_NAME = "net.nurik.roman.muzei"

const val BASE_URL = "http://archillect.com/"

const val KEY_PERMISSION = "permission"

const val EXTENSION_JPG = ".jpg"
const val EXTENSION_PNG = ".png"

const val MINIMUM_WIDTH = 1000
const val MINIMUM_HEIGHT = 1000

private fun buildStyledToast(context: Context, message: String) {
  return StyleableToast.Builder(context)
      .length(Toast.LENGTH_SHORT)
      .text(message)
      .textColor(ContextCompat.getColor(context, R.color.colorAccent))
      .font(R.font.inconsolata)
      .backgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
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

@Suppress("DEPRECATION")
fun isConnectedToWifi(context: Context?): Boolean {
  val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    cm?.getNetworkCapabilities(cm.activeNetwork)?.run {
      hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
  } else {
    cm?.activeNetworkInfo?.run {
      isConnected && type == ConnectivityManager.TYPE_WIFI
    }
  } ?: false
}

fun isExternalStorageWritable() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

fun isPermissionGranted(context: Context, permission: String): Boolean {

  if (ContextCompat.checkSelfPermission(context, permission)
      == PackageManager.PERMISSION_GRANTED) return true

  val i = Intent(context, PermissionRequestActivity::class.java)
  i.putExtra(KEY_PERMISSION, permission)
  i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  context.startActivity(i)
  return false
}

fun getArchillectLink(id: Long): String = BASE_URL + id

fun getRandomLong(bound: Long) = when {
  Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> ThreadLocalRandom.current().nextLong(bound)
  else -> Random().nextInt(bound.toInt()).toLong()
}
