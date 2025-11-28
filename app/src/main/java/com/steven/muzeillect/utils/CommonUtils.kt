@file:JvmName("Utils")

package com.steven.muzeillect.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.core.app.ComponentActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.InputStream

val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

const val MUZEI_PACKAGE_NAME = "net.nurik.roman.muzei"

val BASE_URL = "https://archillect.com/".toUri()

fun tokenUrlForToken(token: String): Uri {
  return BASE_URL.buildUpon().appendPath(token).build()
}

const val KEY_TOKEN = "token"

fun ComponentActivity.showToast(message: String) {
  lifecycleScope.launch(Dispatchers.Main) {
    Toast.makeText(this@showToast, message, Toast.LENGTH_LONG).show()
  }
}

fun InputStream.decodeBitmapOrNull(): Bitmap? {
  try {
    return use { BitmapFactory.decodeStream(it) }
  } catch (_: Exception) {
    return null
  }
}

val Uri.extension: String?
  get() {
    val fileName = lastPathSegment ?: return null
    val split = fileName.splitToSequence(".").asIterable().filter { it.isNotEmpty() }
    if (split.count() < 2) return null
    return split.last()
  }


val Uri.isValidImage: Boolean
  get() {
    val ext = extension ?: return false
    val isValid = when (ext.lowercase()) {
      "jpg", "jpeg", "png", "webp" -> true
      else -> false
    }
    Timber.i("Image extension: $ext, isValid: $isValid")
    return isValid
  }
