package com.steven.muzeillect.utils

import android.graphics.Bitmap
import timber.log.Timber

enum class ImageQuality {
  ANY,
  HD,
  FULL_HD;

  fun validateBitmap(bitmap: Bitmap): Boolean {
    Timber.d("Image height: ${bitmap.height}, width: ${bitmap.width}")
    val minimumPixels = when (this) {
      ANY -> 0.0
      HD -> 720 * 1280 * 0.95
      FULL_HD -> 1080 * 1920 * 0.9
    }
    return bitmap.width * bitmap.height >= minimumPixels
  }
}
