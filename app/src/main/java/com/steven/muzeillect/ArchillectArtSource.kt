package com.steven.muzeillect

import com.google.android.apps.muzei.api.MuzeiArtSource
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource
import com.google.android.apps.muzei.api.UserCommand
import timber.log.Timber
import java.util.Date
import java.util.concurrent.TimeUnit.MINUTES

class ArchillectArtSource : RemoteMuzeiArtSource("ArchillectArtSource") {

  companion object {
    private const val COMMAND_ID_SHARE = 111
    private const val COMMAND_ID_SAVE = 222
  }

  private val archillectCore by lazy {
    ArchillectCore(this, currentArtwork?.token?.toLongOrNull() ?: -1)
  }

  override fun onCreate() {
    super.onCreate()

    Timber.i("Service Created")

    setUserCommands(
        UserCommand(MuzeiArtSource.BUILTIN_COMMAND_ID_NEXT_ARTWORK),
        UserCommand(COMMAND_ID_SAVE, getString(R.string.action_save)),
        UserCommand(COMMAND_ID_SHARE, getString(R.string.action_share))
    )
  }

  override fun onCustomCommand(id: Int) {
    super.onCustomCommand(id)
    when (id) {
      COMMAND_ID_SAVE -> ArchillectCore.saveImage(this, API.OLD, currentArtwork)
      COMMAND_ID_SHARE -> ArchillectCore.shareImage(this, API.OLD, currentArtwork)
    }
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
