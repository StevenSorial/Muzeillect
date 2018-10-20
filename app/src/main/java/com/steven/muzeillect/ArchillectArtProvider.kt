package com.steven.muzeillect

import android.annotation.TargetApi
import android.os.Build
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.apps.muzei.api.MuzeiContract.Artwork.getCurrentArtwork
import com.google.android.apps.muzei.api.UserCommand
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.InputStream
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.TimeUnit.SECONDS

@TargetApi(Build.VERSION_CODES.KITKAT)
class ArchillectArtProvider : MuzeiArtProvider() {

	companion object {
		private const val COMMAND_ID_SHARE = 111
		private const val COMMAND_ID_SAVE = 222
	}

	override fun onLoadRequested(initial: Boolean) {
		Timber.i("load requested")

		val workManager = WorkManager.getInstance()
		val worker = OneTimeWorkRequestBuilder<ArchillectWorker>()
				.setInputData(workDataOf(Pair("oldToken", getCurrentArtwork(context)?.token?.toLongOrNull()
						?: -1)))
				.setConstraints(Constraints.Builder()
						.setRequiredNetworkType(NetworkType.CONNECTED)
						.build())
				.build()
		workManager.enqueue(worker)
	}

	override fun getCommands(artwork: Artwork): List<UserCommand> = context?.run {
		listOf(
				UserCommand(COMMAND_ID_SAVE, context?.getString(R.string.action_save)),
				UserCommand(COMMAND_ID_SHARE, context?.getString(R.string.action_share)))
	} ?: super.getCommands(artwork)

	override fun onCommand(artwork: Artwork, id: Int) {
		context?.run {
			when (id) {
				COMMAND_ID_SAVE -> ArchillectCore.saveImage(this, API.NEW, artwork)
				COMMAND_ID_SHARE -> ArchillectCore.shareImage(this, API.NEW, artwork)
			}
		}
	}

	override fun openFile(artwork: Artwork): InputStream {
		Timber.i("opening file")
		return super.openFile(artwork).also {
			artwork.persistentUri?.toString()?.run {
				try {
					val client = OkHttpClient.Builder().connectTimeout(30, SECONDS).readTimeout(2, MINUTES).build()
					val req = Request.Builder().url(this).build()
					client.newCall(req).execute().body()?.byteStream()
				} catch (e: Exception) {
					Timber.e(e, "Error opening file")
				}

			}
		}
	}
}
