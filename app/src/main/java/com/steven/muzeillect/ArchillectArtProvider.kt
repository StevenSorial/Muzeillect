package com.steven.muzeillect

import android.annotation.TargetApi
import android.app.Application
import android.net.Uri
import android.os.Build
import androidx.preference.PreferenceManager
import androidx.core.content.edit
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.ExistingWorkPolicy
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
    val workManager = WorkManager.getInstance(context!!)
    val worker = OneTimeWorkRequestBuilder<ArchillectWorker>()
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        .build()
    workManager.enqueueUniqueWork("ArchillectArtProvider", ExistingWorkPolicy.REPLACE, worker)
  }

  override fun getCommands(artwork: Artwork): List<UserCommand> {
    if (context == null) return super.getCommands(artwork)
    return context!!.run {
      listOf(
          UserCommand(ArchillectCommands.ID_SAVE, getString(R.string.action_save)),
          UserCommand(ArchillectCommands.ID_SHARE, getString(R.string.action_share)),
          UserCommand(ArchillectCommands.ID_BLACKLIST, getString(R.string.action_blacklist))
      )
    }
  }

  override fun onCommand(artwork: Artwork, id: Int) {
    if (context == null) return
    when (id) {
      ArchillectCommands.ID_SAVE -> ArchillectCommands.save(this, artwork)
      ArchillectCommands.ID_SHARE -> ArchillectCommands.shareImage(this, artwork)
      ArchillectCommands.ID_BLACKLIST -> ArchillectCommands.addToBlacklist(this,artwork)
    }
  }

  override fun openFile(artwork: Artwork): InputStream {
    Timber.i("Opening file")
    return super.openFile(artwork)
  }
}
