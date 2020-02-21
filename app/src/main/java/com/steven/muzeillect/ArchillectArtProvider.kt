package com.steven.muzeillect

import android.annotation.TargetApi
import android.os.Build
import androidx.preference.PreferenceManager
import androidx.core.content.edit
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.workDataOf
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.ExistingWorkPolicy
import com.google.android.apps.muzei.api.MuzeiContract.Artwork.getCurrentArtwork
import com.google.android.apps.muzei.api.UserCommand
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider
import com.google.android.apps.muzei.api.provider.ProviderContract
import timber.log.Timber
import java.io.InputStream

@TargetApi(Build.VERSION_CODES.KITKAT)
class ArchillectArtProvider : MuzeiArtProvider() {

  override fun onLoadRequested(initial: Boolean) {
    Timber.i("load requested")

    val workManager = WorkManager.getInstance()
    val worker = OneTimeWorkRequestBuilder<ArchillectWorker>().apply {
      setInputData(workDataOf(Pair("oldToken", getCurrentArtwork(context)?.token?.toLongOrNull() ?: -1)))
      setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
    }.build()
    workManager.enqueueUniqueWork("ArchillectArtProvider", ExistingWorkPolicy.REPLACE, worker)
  }

  override fun getCommands(artwork: Artwork): List<UserCommand> = context?.run {
    listOf(
        UserCommand(ArchillectCore.COMMAND_ID_SAVE, getString(R.string.action_save)),
        UserCommand(ArchillectCore.COMMAND_ID_SHARE, getString(R.string.action_share)),
        UserCommand(ArchillectCore.COMMAND_ID_BLACKLIST, getString(R.string.action_blacklist)))
  } ?: super.getCommands(artwork)

  override fun onCommand(artwork: Artwork, id: Int) {
    context?.run {
      when (id) {
        ArchillectCore.COMMAND_ID_SAVE -> ArchillectCore.saveImage(this, artwork)
        ArchillectCore.COMMAND_ID_SHARE -> ArchillectCore.shareImage(this, artwork)
        ArchillectCore.COMMAND_ID_BLACKLIST -> addToBlacklist(artwork)
      }
    }
  }

  private fun addToBlacklist(artwork: Artwork) {
    artwork.token?.toLongOrNull() ?: return
    val prefs = PreferenceManager.getDefaultSharedPreferences(context ?: return) ?: return
    val prefKey = context?.getString(R.string.pref_key_blacklist) ?: return
    val originalSet = prefs.getStringSet(prefKey, null) ?: emptySet()
    val newSet = HashSet(originalSet)
    newSet.add(artwork.token)
    prefs.edit { putStringSet(prefKey, newSet) }
    delete(contentUri, "${ProviderContract.Artwork.TOKEN}=?", arrayOf(artwork.token))
  }

  override fun openFile(artwork: Artwork): InputStream {
    Timber.i("Opening file")
    return super.openFile(artwork)
  }
}
