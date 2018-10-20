package com.steven.muzeillect

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.apps.muzei.api.MuzeiContract
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider
import com.google.android.apps.muzei.api.provider.ProviderContract
import timber.log.Timber

@TargetApi(Build.VERSION_CODES.KITKAT)
class ArchillectWorker(val context: Context, workerParams: WorkerParameters)
	: Worker(context, workerParams) {

	private val archillectCore by lazy {
		ArchillectCore(context, inputData.getString("oldToken")?.toLongOrNull() ?: -1)
	}

	override fun doWork(): Result {

		archillectCore.getMaxToken()
		val artwork = archillectCore.getArtwork(API.NEW) as? NewAPIArtwork? ?: return Result.RETRY
		ProviderContract.Artwork.addArtwork(context, "com.steven.muzeillect", artwork)

		return Result.SUCCESS
	}
}
