package com.steven.muzeillect

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import com.google.android.apps.muzei.api.provider.Artwork
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import timber.log.Timber
import java.util.concurrent.ThreadLocalRandom

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
    var response: Response? = null
    try {
      Timber.i("Generating max Token")
      val req = Request.Builder().url(BASE_URL).build()
      response = okHttpClient.newCall(req).execute()
      val docString = response.body?.string()
      val doc = Jsoup.parse(docString!!)
      val element = doc.select("a.post").first()!!.attributes().asList()[1].value
        .removePrefix("/")
      maxToken = element.toLong()
    } catch (e: Exception) {
      Timber.e(e, "Error getting max Token")
      maxToken = -1
    } finally {
      response?.close()
    }
  }

  private fun getImageURL(tokenUrl: String): String? {
    Timber.i("Getting Image URL")
    var response: Response? = null
    try {
      val req = Request.Builder().url(tokenUrl).build()
      response = okHttpClient.newCall(req).execute()
      val docString = response.body?.string()
      val doc = Jsoup.parse(docString!!)
      val img = doc.select("#ii").first()
      val imgUrl = img!!.attr("src")
      Timber.i("Generated Image URL: $imgUrl")
      return imgUrl
    } catch (e: Exception) {
      Timber.e(e, "Error generating Image URL")
      return null
    } finally {
      response?.close()
    }
  }

  private fun validateImageUrl(URLString: String): Uri? {
    Timber.i("Validating Image url")
    var response: Response? = null
    try {
      val req = Request.Builder().url(URLString).build()
      response = okHttpClient.newCall(req).execute()
      val finalUrl = response.request.url.toString().toUri()
      if (response.code != 200) {
        Timber.i("Response code ${response.code}")
        return null
      }
      if (!isImageTypeValid(finalUrl)) {
        Timber.i("Invalid Format")
        return null
      }
      if (finalUrl.toString().contains("media_violation")) {
        Timber.i("Tumblr invalid url: $finalUrl")
        return null
      }
      if (!isHDOnly) return finalUrl
      Timber.i("Checking Image Size")
      val bitmap = BitmapFactory.decodeStream(response.body?.byteStream())
      if (bitmap == null) {
        Timber.e("Decoding Image Failed")
        return null
      }
      val h = bitmap.height
      val w = bitmap.width
      Timber.d("Image height: $h, width: $w")
      if (h * w < MINIMUM_HD_PIXELS) {
        Timber.i("Resolution is low")
        return null
      }
      return finalUrl
    } catch (e: Exception) {
      Timber.e(e, "Error validating image url")
      return null
    } finally {
      response?.close()
    }
  }

  fun getArtwork(): Artwork? {
    if (maxToken <= 0) return null
    val newToken = (ThreadLocalRandom.current().nextLong(maxToken) + 1).toString()
    val tokenUrl = BASE_URL + newToken
    Timber.i("Generated Image Token: $newToken")

    if (blacklist.contains(newToken)) {
      Timber.i("$newToken is blacklisted")
      return getArtwork()
    }

    val imgUrl = getImageURL(tokenUrl) ?: return null
    val finalUrl = validateImageUrl(imgUrl) ?: return getArtwork()
    Timber.i("final url is $finalUrl")

    return Artwork(
      token = newToken,
      title = newToken,
      byline = "Archillect",
      webUri = tokenUrl.toUri(),
      metadata = tokenUrl,
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
