package com.steven.muzeillect

import android.preference.PreferenceManager
import androidx.core.content.edit
import com.google.android.apps.muzei.api.MuzeiArtSource
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource
import com.google.android.apps.muzei.api.UserCommand
import timber.log.Timber
import java.util.Date
import java.util.concurrent.TimeUnit.MINUTES

class ArchillectArtSource : RemoteMuzeiArtSource("ArchillectArtSource") {

  private val archillectCore by lazy {
    ArchillectCore(this, currentArtwork?.token?.toLongOrNull() ?: -1)
  }

  override fun onCreate() {
    super.onCreate()

    Timber.i("Service Created")

    setUserCommands(
        UserCommand(MuzeiArtSource.BUILTIN_COMMAND_ID_NEXT_ARTWORK),
        UserCommand(ArchillectCore.COMMAND_ID_SAVE, getString(R.string.action_save)),
        UserCommand(ArchillectCore.COMMAND_ID_SHARE, getString(R.string.action_share)),
        UserCommand(ArchillectCore.COMMAND_ID_BLACKLIST, getString(R.string.action_blacklist))
    )
  }

  override fun onCustomCommand(id: Int) {
    super.onCustomCommand(id)
    when (id) {
      ArchillectCore.COMMAND_ID_SAVE -> ArchillectCore.saveImage(this, API.OLD, currentArtwork)
      ArchillectCore.COMMAND_ID_SHARE -> ArchillectCore.shareImage(this, API.OLD, currentArtwork)
      ArchillectCore.COMMAND_ID_BLACKLIST -> addToBlacklist(currentArtwork)
    }
  }

  private fun addToBlacklist(artwork: OldAPIArtwork?) {
    artwork?.token?.toLongOrNull() ?: return
    val prefs = PreferenceManager.getDefaultSharedPreferences(this) ?: return
    val prefKey = getString(R.string.pref_key_blacklist) ?: return
    val originalSet = prefs.getStringSet(prefKey, null) ?: emptySet()
    val newSet = HashSet(originalSet)
    newSet.add(artwork.token)
    prefs.edit { putStringSet(prefKey, newSet) }
    onTryUpdate(MuzeiArtSource.UPDATE_REASON_USER_NEXT)
  }

  @Throws(RetryException::class)
  override fun onTryUpdate(reason: Int) {
    Timber.i("Trying to update")

    if (archillectCore.isOnWiFiOnly && !isConnectedToWifi(this)) {
      Timber.i("Update on wifi only..Rescheduling")
      scheduleUpdateAfter(MINUTES.toMillis(RETRY_INTERVAL))
      return
    }

    archillectCore.getMaxToken()

    val artwork = archillectCore.getArtwork(API.OLD) as? OldAPIArtwork?
        ?: throw RetryException()

    publishArtwork(artwork)

    scheduleUpdateAfter(MINUTES.toMillis(archillectCore.updateInterval))
  }

  private fun scheduleUpdateAfter(updateTimeMillis: Long) {
    val time = System.currentTimeMillis() + updateTimeMillis
    Timber.i("Scheduling next artwork at ${Date(time)}")
    scheduleUpdate(time)
  }

}
