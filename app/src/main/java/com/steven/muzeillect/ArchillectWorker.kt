package com.steven.muzeillect

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.apps.muzei.api.provider.ProviderContract

class ArchillectWorker(private val context: Context, workerParams: WorkerParameters)
  : Worker(context, workerParams) {

  private val archillectCore by lazy {
    ArchillectCore(context)
  }

  override fun doWork(): Result {
    archillectCore.getMaxToken()
    val artwork = archillectCore.getArtwork() ?: return Result.retry()
    val provider = ProviderContract.getProviderClient(context, ArchillectArtProvider::class.java)
    provider.addArtwork(artwork)
    return Result.success()
  }
}
