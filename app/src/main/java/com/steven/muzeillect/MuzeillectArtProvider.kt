package com.steven.muzeillect

import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.os.Build
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteActionCompat
import androidx.core.content.edit
import androidx.core.graphics.drawable.IconCompat
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Constraints
import androidx.work.NetworkType
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider
import com.google.android.apps.muzei.api.provider.ProviderContract
import timber.log.Timber
import java.io.InputStream

@TargetApi(Build.VERSION_CODES.KITKAT)
class MuzeillectArtProvider : MuzeiArtProvider() {

  override fun onLoadRequested(initial: Boolean) {
    Timber.i("load requested")
    val context = context ?: return
    val workManager = WorkManager.getInstance(context)
    val work = OneTimeWorkRequestBuilder<MuzeillectWorker>()
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        .build()
    workManager.enqueue(work)
  }

  override fun getCommandActions(artwork: Artwork): List<RemoteActionCompat> {
    val context = context ?: return super.getCommandActions(artwork)
    return listOfNotNull(createShareAction(context, artwork), createBlacklistAction(context, artwork))
  }

  private fun createShareAction(context: Context, artwork: Artwork): RemoteActionCompat? {
    val token = artwork.token ?: return null
    val sendIntent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TEXT, BASE_URL + token.toLong())
      type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, context.getString(R.string.action_share))
    return RemoteActionCompat(
        IconCompat.createWithResource(context, R.drawable.share),
        context.getString(R.string.action_share),
        context.getString(R.string.action_share),
        PendingIntent.getActivity(context, artwork.id.toInt(), shareIntent, 0)
    ).apply { setShouldShowIcon(true) }
  }

  private fun createBlacklistAction(context: Context, artwork: Artwork): RemoteActionCompat? {
    val token = artwork.token ?: return null
    val intent = Intent(context, BlackListReceiver::class.java)
    intent.putExtra(KEY_TOKEN, token)
    return RemoteActionCompat(
        IconCompat.createWithResource(context, R.drawable.blacklist),
        context.getString(R.string.action_blacklist),
        context.getString(R.string.action_blacklist),
        PendingIntent.getBroadcast(context, token.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
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
    val prefs = PreferenceManager.getDefaultSharedPreferences(context) ?: return
    val prefKey = context.getString(R.string.pref_key_blacklist)
    val originalSet = prefs.getStringSet(prefKey, null) ?: emptySet()
    val newSet = HashSet(originalSet)
    newSet.add(token)
    prefs.edit { putStringSet(prefKey, newSet) }
    context.contentResolver?.apply {
      delete(provider.contentUri, "${ProviderContract.Artwork.TOKEN}=?", arrayOf(token))
    }
  }
}
