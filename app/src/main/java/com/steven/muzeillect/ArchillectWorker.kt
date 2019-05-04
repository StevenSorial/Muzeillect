package com.steven.muzeillect

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.apps.muzei.api.provider.ProviderContract

@TargetApi(Build.VERSION_CODES.KITKAT)
class ArchillectWorker(private val context: Context, workerParams: WorkerParameters)
  : Worker(context, workerParams) {

  private val archillectCore by lazy {
    ArchillectCore(context, inputData.getString("oldToken")?.toLongOrNull() ?: -1)
  }

  override fun doWork(): Result {
    archillectCore.getMaxToken()
    val artwork = archillectCore.getArtwork(API.NEW) as? NewAPIArtwork ?: return Result.retry()
    val provider = ProviderContract.getProviderClient(context, ArchillectArtProvider::class.java)
    context.contentResolver.delete(provider.contentUri, "${ProviderContract.Artwork.TOKEN}=?", arrayOf(artwork.token))
    provider.addArtwork(artwork)
    return Result.success()
  }
}
