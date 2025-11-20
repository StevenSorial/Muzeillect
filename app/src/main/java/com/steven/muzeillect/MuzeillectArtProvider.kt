package com.steven.muzeillect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteActionCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider
import com.google.android.apps.muzei.api.provider.ProviderContract
import timber.log.Timber
import java.io.InputStream
import android.app.PendingIntent.*
import androidx.datastore.preferences.core.edit
import com.steven.muzeillect.utils.KEY_TOKEN
import com.steven.muzeillect.utils.PrefsKey
import com.steven.muzeillect.utils.appScope
import com.steven.muzeillect.utils.settingsDataStore
import com.steven.muzeillect.utils.tokenUrlForToken
import kotlinx.coroutines.launch

class MuzeillectArtProvider : MuzeiArtProvider() {

  override fun onLoadRequested(initial: Boolean) {
    Timber.i("load requested")
    val context = context ?: return
    val workManager = WorkManager.getInstance(context)
    val tag = "muzeillect"
    workManager.cancelAllWorkByTag(tag)
    val work = OneTimeWorkRequestBuilder<MuzeillectWorker>()
      .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
      .addTag(tag)
      .build()
    workManager.enqueue(work)
  }

  override fun getCommandActions(artwork: Artwork): List<RemoteActionCompat> {
    val context = context ?: return super.getCommandActions(artwork)
    return listOfNotNull(
      createShareAction(context, artwork),
      createBlacklistAction(context, artwork)
    )
  }

  private fun createShareAction(context: Context, artwork: Artwork): RemoteActionCompat? {
    val token = artwork.token ?: return null
    val sendIntent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TEXT, tokenUrlForToken(token).toString())
      type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, context.getString(R.string.action_share))
    val flag = FLAG_IMMUTABLE

    return RemoteActionCompat(
      IconCompat.createWithResource(context, R.drawable.share),
      context.getString(R.string.action_share),
      context.getString(R.string.action_share),
      getActivity(context, artwork.id.toInt(), shareIntent, flag)
    ).apply { setShouldShowIcon(true) }
  }

  private fun createBlacklistAction(context: Context, artwork: Artwork): RemoteActionCompat? {
    val token = artwork.token ?: return null
    val intent = Intent(context, BlackListReceiver::class.java)
    intent.putExtra(KEY_TOKEN, token)
    val flag = FLAG_IMMUTABLE or FLAG_CANCEL_CURRENT

    return RemoteActionCompat(
      IconCompat.createWithResource(context, R.drawable.blacklist),
      context.getString(R.string.action_block),
      context.getString(R.string.action_block),
      getBroadcast(context, token.toInt(), intent, flag)
    ).apply { setShouldShowIcon(false) }
  }

  override fun openFile(artwork: Artwork): InputStream {
    Timber.i("Opening file")
    return super.openFile(artwork)
  }
}

class BlackListReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    if (context == null) return
    val token = intent?.getStringExtra(KEY_TOKEN) ?: return
    val provider = ProviderContract.getProviderClient<MuzeillectArtProvider>(context)

    appScope.launch {
      context.settingsDataStore.edit {
        val prefKey = PrefsKey.BlockList
        val currentSet = prefKey.getFrom(it)
        prefKey.setIn(it, currentSet + token)
      }

      context.contentResolver?.apply {
        delete(provider.contentUri, "${ProviderContract.Artwork.TOKEN}=?", arrayOf(token))
      }
    }
  }
}
