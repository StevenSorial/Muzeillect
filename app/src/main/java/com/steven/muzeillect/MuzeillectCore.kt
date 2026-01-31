package com.steven.muzeillect

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import com.google.android.apps.muzei.api.provider.Artwork
import com.steven.muzeillect.utils.ImageQuality
import com.steven.muzeillect.utils.PrefsKey
import com.steven.muzeillect.utils.isValidImage
import com.steven.muzeillect.utils.settingsDataStore
import com.steven.muzeillect.utils.tokenUrlForToken
import com.steven.muzeillect.utils.useOrNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

class MuzeillectCore(private val context: Context) {

  private val selectedQualityFlow =
    context.settingsDataStore.data.map { PrefsKey.SelectedQuality.getFrom(it) }

  private val blacklistFlow =
    context.settingsDataStore.data.map { PrefsKey.BlockList.getFrom(it) }

  private suspend fun generateMaxToken(): Long? {
    try {
      Timber.i("Generating max Token")
      val found = NetworkClient.getMaxToken()
      Timber.i("Found max Token: $found")
      return found
    } catch (e: Exception) {
      Timber.e(e, "Error getting max Token")
      return null
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
      if (!finalUrl.isValidImage) {
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
      val bitmap: Bitmap? = response.body.byteStream().useOrNull {
        BitmapFactory.decodeStream(it)
      }
      if (bitmap == null || !selectedQuality.validateSize(bitmap)) {
        Timber.i("Resolution is low")
        return@getImageData null
      }
      return@getImageData finalUrl
    }
  }

  suspend fun getArtwork(): Artwork? {
    val maxToken = generateMaxToken() ?: return null
    val newToken = (1L..maxToken).random().toString()
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
      byline = context.getString(R.string.app_name),
      webUri = tokenUrl,
      metadata = tokenUrl.toString(),
      persistentUri = finalUrl
    )
  }
}
