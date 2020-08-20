package com.steven.muzeillect

import android.content.Context
import android.graphics.BitmapFactory
import androidx.preference.PreferenceManager
import androidx.core.net.toUri
import com.google.android.apps.muzei.api.provider.Artwork
import okhttp3.Request
import org.jsoup.Jsoup
import timber.log.Timber
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.max
import kotlin.math.min

class MuzeillectCore(private val context: Context) {

  private val pref by lazy {
    PreferenceManager.getDefaultSharedPreferences(context)
  }

  private val isHDOnly: Boolean
    get() = pref.getBoolean(context.getString(R.string.pref_key_hd), false)

  private val blacklist: Set<String>
    get() = pref.getStringSet(context.getString(R.string.pref_key_blacklist), null) ?: emptySet()

  private var maxToken: Long = -1

  fun generateMaxToken() {
    try {
      Timber.i("Generating max Token")
      val req = Request.Builder().url(BASE_URL).build()
      val response = okHttpClient.newCall(req).execute()
      val docString = response.body?.string()
      response.close()
      val doc = Jsoup.parse(docString)
      val element = doc.select("a.post").first().attributes().asList()[1].value
          .removePrefix("/")
      maxToken = element.toLong()
    } catch (e: Exception) {
      Timber.e(e, "Error getting max Token")
      maxToken = -1
    }
  }

  private fun getImageURL(token: Long): String? {
    Timber.i("Getting Image URL")
    try {
      val req = Request.Builder().url(BASE_URL + token).build()
      val response = okHttpClient.newCall(req).execute()
      val docString = response.body?.string()
      response.close()
      val doc = Jsoup.parse(docString)
      val img = doc.select("#ii").first()
      val imgUrl = img.attr("src")
      Timber.i("Generated Image URL: $imgUrl")
      return imgUrl
    } catch (e: Exception) {
      Timber.e(e, "Error generating Image URL")
      return null
    }
  }

  private fun isImageValid(URLString: String): Boolean {
    Timber.i("Validating Image")
    if (!isImageTypeValid(URLString)) {
      Timber.i("Invalid Format")
      return false
    }
    try {
      val req = Request.Builder().url(URLString).build()
      val response = okHttpClient.newCall(req).execute()
      val responseCode = response.code
      Timber.i("Response code $responseCode")
      if (responseCode != 200) return false
      if (!isHDOnly) return true
      Timber.i("Checking Image Size")
      val bitmap = BitmapFactory.decodeStream(response.body?.byteStream())
      response.close()
      if (bitmap == null) {
        Timber.e("Decoding Image Failed")
        return false
      }
      val h = max(bitmap.height, bitmap.width)
      val w = min(bitmap.height, bitmap.width)
      Timber.d("Image Resolution: $w x $h")
      if (h < MINIMUM_HD_HEIGHT || w < MINIMUM_HD_WIDTH) {
        Timber.i("Resolution is low")
        return false
      }
    } catch (e: Exception) {
      Timber.e(e, "Error Checking Image Size")
      return false
    }
    return true
  }

  fun getArtwork(): Artwork? {
    if (maxToken <= 0) return null
    val newToken = ThreadLocalRandom.current().nextLong(maxToken) + 1

    Timber.i("Generated Image Token: $newToken")

    if (blacklist.contains(newToken.toString())) {
      Timber.i("$newToken is blacklisted")
      return getArtwork()
    }

    val imgUrl = getImageURL(newToken) ?: return null

    if (!isImageValid(imgUrl)) {
      return getArtwork()
    }

    val token = newToken.toString()
    return Artwork(
        token = token,
        title = token,
        byline = "Archillect",
        webUri = (BASE_URL + token.toLong()).toUri(),
        metadata = (BASE_URL + token.toLong()),
        persistentUri = imgUrl.toUri()
    )
  }

  private fun isImageTypeValid(imgURL: String): Boolean {
    return when (imgURL.toUri().extension) {
      "jpg", "jpeg", "png" -> true
      else -> false
    }
  }
}
