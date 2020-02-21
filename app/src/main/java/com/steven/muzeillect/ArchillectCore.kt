package com.steven.muzeillect

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Environment
import androidx.preference.PreferenceManager
import androidx.core.net.toUri
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.MuzeiContract
import okhttp3.Request
import org.jsoup.Jsoup
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.thread
import kotlin.math.max
import kotlin.math.min

class ArchillectCore(private val context: Context?, private val oldToken: Long = -1) {

  private val pref by lazy {
    PreferenceManager.getDefaultSharedPreferences(context)
  }

  private val isHDOnly by lazy {
    pref.getBoolean(context?.getString(R.string.pref_key_hd), false)
  }

  private val blacklist by lazy {
    pref.getStringSet(context?.getString(R.string.pref_key_blacklist), null) ?: emptySet()
  }

  private var maxToken: Long = -1

  fun getMaxToken() {
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
    Timber.i("Generating Image Token")
    try {
      val req = Request.Builder().url(getArchillectLink(token)).build()
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
    if (!isJPGOrPNG(URLString)) {
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

    val newToken = if (maxToken > 0) getRandomLong(maxToken) + 1 else return null

    Timber.i("Generated Image Token: $newToken")

    if (oldToken == newToken) {
      Timber.i("New token is the Same as old one")
      return getArtwork()
    }

    if (blacklist.contains(newToken.toString())) {
      Timber.i("$newToken is blacklisted")
      Timber.e(blacklist.toString())
      return getArtwork()
    }

    val imgUrl = getImageURL(newToken) ?: return null

    if (!isImageValid(imgUrl)) {
      return getArtwork()
    }

    return Artwork().apply {
      token = newToken.toString()
      title = newToken.toString()
      byline = "Archillect"
      webUri = getArchillectLink(newToken).toUri()
      metadata = getArchillectLink(newToken)
      persistentUri = imgUrl.toUri()
    }
  }

  companion object {
    const val COMMAND_ID_SHARE = 111
    const val COMMAND_ID_SAVE = 222
    const val COMMAND_ID_BLACKLIST = 333

    fun saveImage(context: Context, artwork: Artwork?) {
      if (artwork == null) {
        Timber.e("No artwork available to save")
        return
      }
      val uri = artwork.persistentUri
      val token = artwork.token
      if (!isExternalStorageWritable()) {
        Timber.e("Storage is Not Writable")
        return
      }

      if (!isPermissionGranted(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
        showToast(context, context.getString(R.string.message_permission_not_granted))
        return
      }

      thread {
        Timber.i("Saving Image")
        val format: Bitmap.CompressFormat
        val ext: String
        if (isPNG(uri.toString())) {
          format = Bitmap.CompressFormat.PNG
          ext = EXTENSION_PNG
        } else {
          format = Bitmap.CompressFormat.JPEG
          ext = EXTENSION_JPG
        }
        val os: FileOutputStream
        val bitmap = MuzeiContract.Artwork.getCurrentArtworkBitmap(context)
        val folder = File(Environment.getExternalStoragePublicDirectory(Environment
            .DIRECTORY_PICTURES), "Archillect")
        if (!folder.exists()) {
          Timber.i("creating directory")
          folder.mkdirs()
        }
        val file = File(folder, token + ext)
        if (file.exists()) {
          Timber.i("File already exists")
          showToast(context, context.getString(R.string.message_save_exists))
          return@thread
        }
        try {
          os = FileOutputStream(file)
          val saved = bitmap.compress(format, 100, os)
          os.flush()
          os.close()
          if (saved) {
            Timber.i("Saving Finished")
            showToast(context, context.getString(R.string.message_save_complete))
            MediaScannerConnection.scanFile(context,
                arrayOf(file.toString()), null
            ) { path, uri ->
              Timber.d("Scanned: $path")
              Timber.d("-> uri: $uri")
            }
          } else {
            Timber.e("Saving Error")
            showToast(context, context.getString(R.string.message_save_error))
          }
        } catch (e: Exception) {
          Timber.e(e, "Saving Error")
          showToast(context, context.getString(R.string.message_save_error))
        }
      }
    }

    fun shareImage(context: Context, artwork: Artwork?) {
      if (artwork == null) {
        Timber.e("No artwork available to share")
        return
      }
      val token = artwork.token!!
      val i = Intent()
      i.action = Intent.ACTION_SEND
      i.putExtra(Intent.EXTRA_TEXT, getArchillectLink(token.toLong()))
      i.type = "text/plain"
      context.startActivity(Intent.createChooser(i, context.getString(R.string.action_share))
          .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
  }
}

private fun isJPG(imgURL: String) = imgURL.toLowerCase().contains(EXTENSION_JPG) || imgURL.toLowerCase().contains(EXTENSION_JPEG)

private fun isPNG(imgURL: String) = imgURL.toLowerCase().contains(EXTENSION_PNG)

fun isJPGOrPNG(imgURL: String): Boolean {
  return isJPG(imgURL) || isPNG(imgURL)
}
