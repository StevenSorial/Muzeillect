package com.steven.muzeillect

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.apps.muzei.api.provider.ProviderContract

class MuzeillectWorker(private val context: Context, workerParams: WorkerParameters) :
  Worker(context, workerParams) {

  override fun doWork(): Result {
    val core = MuzeillectCore(context)
    core.generateMaxToken()
    val artwork = core.getArtwork() ?: return Result.retry()
    val provider = ProviderContract.getProviderClient<MuzeillectArtProvider>(context)
    provider.addArtwork(artwork)
    onlyKeepLast100()
    return Result.success()
  }

  private fun onlyKeepLast100() {
    val contentResolver = context.contentResolver ?: return
    val provider = ProviderContract.getProviderClient<MuzeillectArtProvider>(context)
    val cursorForIds = contentResolver.query(
      provider.contentUri,
      arrayOf(ProviderContract.Artwork.TOKEN),
      null,
      null,
      "${ProviderContract.Artwork.DATE_MODIFIED} DESC"
    ) ?: return
    val idArray = arrayListOf<String>()
    while (cursorForIds.moveToNext()) {
      idArray.add(cursorForIds.getString(0))
    }
    cursorForIds.close()
    val remainingTokens = idArray.drop(100)
    remainingTokens.forEach {
      contentResolver.delete(provider.contentUri,"${ProviderContract.Artwork.TOKEN}=?", arrayOf(it))
    }
  }
}
