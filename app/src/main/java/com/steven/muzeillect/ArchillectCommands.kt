package com.steven.muzeillect

import android.Manifest
import android.annotation.TargetApi
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.IS_PENDING
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.android.apps.muzei.api.MuzeiContract
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import okio.source
import timber.log.Timber
import java.io.File
import java.lang.RuntimeException

object ArchillectCommands {
  const val ID_SHARE = 111
  const val ID_SAVE = 222
  const val ID_BLACKLIST = 333

  fun addToBlacklist(provider: ArchillectArtProvider, artwork: Artwork) {
    val context = provider.context!!
    val prefs = PreferenceManager.getDefaultSharedPreferences(context) ?: return
    val prefKey = context.getString(R.string.pref_key_blacklist)
    val originalSet = prefs.getStringSet(prefKey, null) ?: emptySet()
    val newSet = HashSet(originalSet)
    newSet.add(artwork.token!!)
    prefs.edit { putStringSet(prefKey, newSet) }
    provider.apply {
      delete(contentUri, "${ProviderContract.Artwork.TOKEN}=?", arrayOf(artwork.token!!))
    }
  }

  fun saveImage(provider: ArchillectArtProvider, artwork: Artwork) {
    val context = provider.context!!
    when {
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> saveQ(context, artwork)
      else -> saveLegacy(context, artwork)
    }
  }

  @TargetApi(Build.VERSION_CODES.Q)
  private fun saveQ(context: Context, artwork: Artwork) {
    GlobalScope.launch {
      saveQImpl(context, artwork)
    }
  }

  @TargetApi(Build.VERSION_CODES.Q)
  private suspend fun saveQImpl(context: Context, artwork: Artwork) {
    if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
      Timber.e("Storage is Not Writable")
      return context.saveImplFailed()
    }
    withContext(Dispatchers.IO) {
      Timber.d("Saving Image for Q")
      val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, artwork.token!!)
        put(MediaStore.MediaColumns.MIME_TYPE, artwork.mimeType)
        put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Archillect")
        put(IS_PENDING, 1)
      }
      val resolver = context.contentResolver

      Timber.d("inserting contentValues")
      val uri = resolver.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL), contentValues)
      if (uri == null) {
        Timber.e("Saving Image Uri is null")
        return@withContext context.saveImplFailed()
      }
      Timber.d("opening outputStream")
      val outputStream = try { resolver.openOutputStream(uri) } catch (e: Exception) { Timber.e(e); null }
          ?: return@withContext context.saveImplFailed(uri)
      val inputUri = MuzeiContract.Artwork.CONTENT_URI
      Timber.d("opening inputStream")
      val inputStream = try { resolver.openInputStream(inputUri) } catch (e: Exception) { Timber.e(e); null }
          ?: return@withContext context.saveImplFailed(uri)
      try {
        val sink = outputStream.sink().buffer()
        inputStream.source().let { sink.writeAll(it) }
        sink.close()
      } catch (e: Exception) {
        Timber.e(e,"Saving Image Okio error")
        return@withContext context.saveImplFailed(uri)
      }
      Timber.d("image saved")
      context.showToast(context.getString(R.string.message_save_complete))
      resolver.update(uri, ContentValues().apply { put(IS_PENDING, 0) }, null, null)
    }
  }

  private fun saveLegacy(context: Context, artwork: Artwork) {
    val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
    val isPermissionGranted = context.isPermissionGranted(permission)
    if (!isPermissionGranted) {
      context.showToast(context.getString(R.string.message_permission_grant_first))
      val i = Intent(context, PermissionRequestActivity::class.java)
      i.putExtra(KEY_PERMISSION, permission)
      i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(i)
    } else {
      GlobalScope.launch {
        saveLegacyImpl(context, artwork)
      }
    }
  }

  @Suppress("DEPRECATION")
  private suspend fun saveLegacyImpl(context: Context, artwork: Artwork) {
    if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
      Timber.e("Storage is Not Writable")
      return context.showToast(context.getString(R.string.message_save_error))
    }
    Timber.d("Saving Image for Legacy")
    withContext(Dispatchers.IO) {
      val folder = File(
          Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
          "Archillect"
      )
      try {
        Timber.d("Creating folder")
        folder.mkdirs()
      } catch (e: Exception){
        Timber.e(e,"folder creation failed")
        return@withContext context.showToast(context.getString(R.string.message_save_error))
      }
      val file = File(folder, artwork.fileName)
      val resolver = context.contentResolver
      val inputUri = MuzeiContract.Artwork.CONTENT_URI
      Timber.d("opening inputStream")
      val inputStream = try { resolver.openInputStream(inputUri) } catch (e: Exception) { Timber.e(e); null }
          ?: return@withContext context.showToast(context.getString(R.string.message_save_error))
      try {
        val sink = file.sink().buffer()
        inputStream.source().let { sink.writeAll(it) }
        sink.close()
      } catch (e: Exception) {
        Timber.e(e, "Saving Image Okio error")
        return@withContext context.showToast(context.getString(R.string.message_save_error))
      }
      Timber.d("image saved")
      context.showToast(context.getString(R.string.message_save_complete))
      MediaScannerConnection.scanFile(
          context,
          arrayOf(file.absolutePath),
          null,
          null
      )
    }
  }

  @TargetApi(Build.VERSION_CODES.Q)
  private fun Context.saveImplFailed(uri: Uri? = null) {
    uri?.let {
      val contentValues = ContentValues().apply { put(IS_PENDING, 0) }
      contentResolver.update(it, contentValues, null, null)
    }
    showToast(getString(R.string.message_save_error))
  }

  private val Artwork.mimeType: String
    get() {
      return when (val ext = persistentUri!!.extension) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        else -> throw RuntimeException("Should not happen. Unexpected artwork extension $ext")
      }
    }

  private val Artwork.fileName: String
    get() {
      var ext = persistentUri!!.extension!!
      if (ext == "jpeg") ext = "jpg"
      return token!! + "." + ext
    }
}
