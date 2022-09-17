package com.steven.muzeillect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.RemoteActionCompat
import androidx.core.content.edit
import androidx.core.graphics.drawable.IconCompat
import androidx.preference.PreferenceManager
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
      putExtra(Intent.EXTRA_TEXT, BASE_URL + token.toLong())
      type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, context.getString(R.string.action_share))
    val flag =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) FLAG_IMMUTABLE
      else 0

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
    val flag =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) FLAG_IMMUTABLE or FLAG_CANCEL_CURRENT
      else FLAG_CANCEL_CURRENT

    return RemoteActionCompat(
      IconCompat.createWithResource(context, R.drawable.blacklist),
      context.getString(R.string.action_blacklist),
      context.getString(R.string.action_blacklist),
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
    val prefs = PreferenceManager.getDefaultSharedPreferences(context) ?: return
    val prefKey = context.getString(R.string.pref_key_blacklist)
    val originalSet = prefs.getStringSet(prefKey, null) ?: emptySet()
    val newSet = HashSet(originalSet)
    newSet.add(token)
    prefs.edit(true) { putStringSet(prefKey, newSet) }
    context.contentResolver?.apply {
      delete(provider.contentUri, "${ProviderContract.Artwork.TOKEN}=?", arrayOf(token))
    }
  }
}
