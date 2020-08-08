package com.steven.muzeillect

import android.annotation.TargetApi
import android.app.PendingIntent
import android.os.Build
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteActionCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.ExistingWorkPolicy
import com.google.android.apps.muzei.api.UserCommand
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider
import timber.log.Timber
import java.io.InputStream

@TargetApi(Build.VERSION_CODES.KITKAT)
class ArchillectArtProvider : MuzeiArtProvider() {

  override fun onLoadRequested(initial: Boolean) {
    Timber.i("load requested")
    val context = context ?: return
    val workManager = WorkManager.getInstance(context)
    val worker = OneTimeWorkRequestBuilder<ArchillectWorker>()
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        .build()
    workManager.enqueueUniqueWork("ArchillectArtProvider", ExistingWorkPolicy.REPLACE, worker)
  }

  override fun getCommands(artwork: Artwork): List<UserCommand> {
    val context = context ?: return super.getCommands(artwork)
    return context.run {
      listOf(
          UserCommand(ArchillectCommands.ID_SAVE, getString(R.string.action_save)),
          UserCommand(ArchillectCommands.ID_BLACKLIST, getString(R.string.action_blacklist))
      )
    }
  }

  override fun onCommand(artwork: Artwork, id: Int) {
    when (id) {
      ArchillectCommands.ID_SAVE -> ArchillectCommands.saveImage(this, artwork)
      ArchillectCommands.ID_BLACKLIST -> ArchillectCommands.addToBlacklist(this, artwork)
    }
  }

  override fun getCommandActions(artwork: Artwork): List<RemoteActionCompat> {
    val context = context ?: return super.getCommandActions(artwork)
    val oldActions = super.getCommandActions(artwork)
    val newActions = listOfNotNull(createShareAction(context, artwork))
    return oldActions + newActions
  }

  private fun createShareAction(context: Context, artwork: Artwork): RemoteActionCompat? {
    val token = artwork.token ?: return null
    val sendIntent = Intent()
    sendIntent.action = Intent.ACTION_SEND
    sendIntent.putExtra(Intent.EXTRA_TEXT, BASE_URL + token.toLong())
    sendIntent.type = "text/plain"
    val shareIntent = Intent.createChooser(sendIntent, context.getString(R.string.action_share))
    return RemoteActionCompat(
        IconCompat.createWithResource(context, R.drawable.share),
        context.getString(R.string.action_share),
        context.getString(R.string.action_share),
        PendingIntent.getActivity(context, artwork.id.toInt(), shareIntent, 0)
    )
  }

  override fun openFile(artwork: Artwork): InputStream {
    Timber.i("Opening file")
    return super.openFile(artwork)
  }
}
