package com.steven.muzeillect

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import com.google.android.apps.muzei.api.provider.Artwork
import com.steven.muzeillect.utils.ImageQuality
import com.steven.muzeillect.utils.PrefsKey
import com.steven.muzeillect.utils.extension
import com.steven.muzeillect.utils.settingsDataStore
import com.steven.muzeillect.utils.tokenUrlForToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.concurrent.ThreadLocalRandom

class MuzeillectCore(context: Context) {

  private val selectedQualityFlow =
    context.settingsDataStore.data.map { PrefsKey.SelectedQuality.getFrom(it) }

  private val blacklistFlow =
    context.settingsDataStore.data.map { PrefsKey.DenyList.getFrom(it) }

  private var maxToken: Long = -1

  suspend fun generateMaxToken() {
    try {
      Timber.i("Generating max Token")
      maxToken = NetworkClient.getMaxToken()
    } catch (e: Exception) {
      Timber.e(e, "Error getting max Token")
      maxToken = -1
    }
  }

  private suspend fun getImageURL(tokenUrl: Uri): Uri? {
    try {
      Timber.i("Getting Image URL")
      val imgUrl = NetworkClient.getImageUrl(tokenUrl)
      Timber.i("Generated Image URL: $imgUrl")
      return imgUrl
    } catch (e: Exception) {
      Timber.e(e, "Error generating Image URL")
      return null
    }
  }

  private suspend fun validateImageUrl(url: Uri): Uri? {
    Timber.i("Validating Image url")

    return NetworkClient.getImageData(url) { response ->
      val finalUrl = response.request.url.toString().toUri()
      if (response.code != 200) {
        Timber.i("Response code ${response.code}")
        return@getImageData null
      }
      if (!isImageTypeValid(finalUrl)) {
        Timber.i("Invalid Format")
        return@getImageData null
      }
      if (finalUrl.toString().contains("media_violation")) {
        Timber.i("Tumblr invalid url: $finalUrl")
        return@getImageData null
      }

      val selectedQuality = selectedQualityFlow.first()
      if (selectedQuality == ImageQuality.ANY) return@getImageData finalUrl
      Timber.i("Checking Image Size")
      val bitmap: Bitmap? = BitmapFactory.decodeStream(response.body.byteStream())
      if (bitmap == null || !selectedQuality.validateBitmap(bitmap)) {
        Timber.i("Resolution is low")
        return@getImageData null
      }
      return@getImageData finalUrl
    }
  }

  suspend fun getArtwork(): Artwork? {
    if (maxToken <= 0) return null
    val newToken = (ThreadLocalRandom.current().nextLong(maxToken) + 1).toString()
    val tokenUrl = tokenUrlForToken(newToken)
    Timber.i("Generated Image Token: $newToken")

    val blacklist = blacklistFlow.first()
    if (blacklist.contains(newToken)) {
      Timber.i("$newToken is already in the denylist")
      return getArtwork()
    }

    val imgUrl = getImageURL(tokenUrl) ?: return null
    val finalUrl = validateImageUrl(imgUrl) ?: return getArtwork()
    Timber.i("final url is $finalUrl")

    return Artwork(
      token = newToken,
      title = newToken,
      byline = "Archillect",
      webUri = tokenUrl,
      metadata = tokenUrl.toString(),
      persistentUri = finalUrl
    )
  }

  private fun isImageTypeValid(imgURL: Uri): Boolean {
    return when (imgURL.extension?.lowercase()) {
      "jpg", "jpeg", "png" -> true
      else -> false
    }
  }
}
