package com.steven.muzeillect

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.preference.PreferenceManager
import com.google.android.apps.muzei.api.Artwork
import com.google.android.apps.muzei.api.MuzeiArtSource
import com.google.android.apps.muzei.api.MuzeiContract
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource
import com.google.android.apps.muzei.api.UserCommand
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.concurrent.thread

class ArchillectArtSource : RemoteMuzeiArtSource("ArchillectArtSource") {

	companion object {
		private const val COMMAND_ID_SHARE = 111
		private const val COMMAND_ID_SAVE = 222
	}

	private val pref by lazy {
		PreferenceManager.getDefaultSharedPreferences(this)
	}
	private val updateInterval by lazy {
		pref.getString(getString(R.string.pref_key_interval),
				getString(R.string.pref_interval_value_default))
				?.toLongOrNull() ?: 180
	}
	private val isOnWiFiOnly by lazy {
		pref.getBoolean(getString(R.string.pref_key_wifi), false)
	}
	private val isHDOnly by lazy {
		pref.getBoolean(getString(R.string.pref_key_hd), false)
	}

	private val okHttpClient by lazy {
		 OkHttpClient.Builder().connectTimeout(30, SECONDS).readTimeout(2, MINUTES).build()
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
			COMMAND_ID_SAVE -> saveImage()
			COMMAND_ID_SHARE -> shareImage()
		}
	}

	@Throws(RetryException::class)
	override fun onTryUpdate(reason: Int) {

		Timber.i("Trying to update")

		if (isOnWiFiOnly && !isConnectedToWifi(this)) {
			Timber.i("Update on wifi only..Rescheduling")
			scheduleUpdateAfter(MINUTES.toMillis(RETRY_INTERVAL))
			return
		}

		val oldToken = currentArtwork?.token?.toLongOrNull() ?: -1
		Timber.i("old token = $oldToken")

		val newToken = getRandomToken()
		if (oldToken == newToken) {
			Timber.i("New token is the Same as old one")
			throw getRetryException()
		}

		val imgUrl = getImageURL(newToken)
		if (!isJPGOrPNG(imgUrl)) {
			Timber.i("Invalid Format")
			throw getRetryException()
		}

		if (getResponseCode(imgUrl) != 200) {
			throw getRetryException()
		}

		if (isHDOnly && !isImageHD(imgUrl)) {
			Timber.i("Resolution is low")
			throw getRetryException()
		}

		publishArtwork(Artwork.Builder()
				.title(newToken.toString())
				.byline("Archillect")
				.imageUri(Uri.parse(imgUrl))
				.token(newToken.toString())
				.viewIntent(Intent(Intent.ACTION_VIEW, Uri.parse(getArchillectLink(newToken))))
				.build())
		scheduleUpdateAfter(MINUTES.toMillis(updateInterval))
	}

	@Throws(RetryException::class)
	private fun getRandomToken(): Long {
		Timber.i("Generating Image Token")
		try {
			val req = Request.Builder().url(BASE_URL).build()
			val docString = okHttpClient.newCall(req).execute().body()?.string()
			val doc = Jsoup.parse(docString)
			val element = doc.select("div.overlay").first()
			val lastToken = element.text().toLong()
			val randToken = getRandomLong(lastToken) + 1
			Timber.i("Generated Image Token: $randToken")
			return randToken
		} catch (e: Exception) {
			Timber.e(e, "Error generating Token")
			throw getRetryException()
		}
	}

	@Throws(RetryException::class)
	private fun getImageURL(token: Long): String {
		Timber.i("Generating Image Token")
		try {
			val req = Request.Builder().url(getArchillectLink(token)).build()
			val docString = okHttpClient.newCall(req).execute().body()?.string()
			val doc = Jsoup.parse(docString)
			val img = doc.select("#ii").first()
			val imgUrl = img.attr("src")
			Timber.i("Generated Image URL: $imgUrl")
			return imgUrl
		} catch (e: Exception) {
			Timber.e(e, "Error generating Image URL")
			throw getRetryException()
		}
	}

	private fun getResponseCode(URL: String): Int {
		Timber.i("Getting response code")
		try {
			val req = Request.Builder().url(URL).build()
			val responseCode = okHttpClient.newCall(req).execute().code()
			Timber.i("response code: $responseCode")
			return responseCode
		} catch (e: Exception) {
			Timber.e(e, "Error trying connecting to url")
			return -1
		}
	}

	private fun isImageHD(URLString: String): Boolean {
		Timber.i("Checking Image Size")
		try {
			val req = Request.Builder().url(URLString).build()
			val stream = okHttpClient.newCall(req).execute().body()?.byteStream()
			val bitmap = BitmapFactory.decodeStream(stream)
			if (bitmap == null) {
				Timber.e("Decoding Image Failed")
				return false
			}
			val h = bitmap.height
			val w = bitmap.width
			Timber.d("Image Resolution: $w x $h")
			bitmap.recycle()
			if (h >= MINIMUM_HEIGHT && w >= MINIMUM_WIDTH) {
				return true
			}
		} catch (e: Exception) {
			Timber.e(e, "Checking Image Size")
		}
		return false
	}

	private fun saveImage() {
		if (currentArtwork?.token == null) {
			Timber.e("No artwork available to save")
			return
		}

		if (!isExternalStorageWritable()) {
			Timber.e("Storage is Not Writable")
			return
		}

		if (!isPermissionGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			showToast(this, getString(R.string.message_permission_not_granted))
			return
		}

		thread {
			Timber.i("Saving Image")
			val format: Bitmap.CompressFormat
			val ext: String
			if (isPNG(currentArtwork.imageUri.toString())) {
				format = Bitmap.CompressFormat.PNG
				ext = EXTENSION_PNG
			} else {
				format = Bitmap.CompressFormat.JPEG
				ext = EXTENSION_JPG
			}
			val os: FileOutputStream
			val bitmap = MuzeiContract.Artwork.getCurrentArtworkBitmap(this)
			val folder = File(Environment.getExternalStoragePublicDirectory(Environment
					.DIRECTORY_PICTURES), "Archillect")
			if (!folder.exists()) {
				Timber.i("creating directory")
				folder.mkdirs()
			}
			val file = File(folder, currentArtwork.token + ext)
			if (file.exists()) {
				Timber.i("File already exists")
				showToast(this, getString(R.string.message_save_exists))
				return@thread
			}
			try {
				os = FileOutputStream(file)
				val saved = bitmap.compress(format, 100, os)
				os.flush()
				os.close()
				if (saved) {
					Timber.i("Saving Finished")
					showToast(this, getString(R.string.message_save_complete))
					MediaScannerConnection.scanFile(this,
							arrayOf(file.toString()), null
					) { path, uri ->
						Timber.d("Scanned: $path")
						Timber.d("-> uri: $uri")
					}
				} else {
					Timber.e("Saving Error")
					showToast(this, getString(R.string.message_save_error))
				}
			} catch (e: Exception) {
				Timber.e(e, "Saving Error")
				showToast(this, getString(R.string.message_save_error))
			}
		}
	}

	private fun shareImage() {
		if (currentArtwork?.token == null) {
			Timber.e("No artwork available to share")
			return
		}
		val i = Intent()
		i.action = Intent.ACTION_SEND
		i.putExtra(Intent.EXTRA_TEXT, getArchillectLink(currentArtwork.token.toLong()))
		i.type = "text/plain"
		startActivity(Intent.createChooser(i, getString(R.string.action_share))
				.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
	}

	private fun scheduleUpdateAfter(updateTimeMillis: Long) {
		val time = System.currentTimeMillis() + updateTimeMillis
		Timber.i("Scheduling next artwork at ${Date(time)}")
		scheduleUpdate(time)
	}

	private fun getRetryException(): RetryException {
		Timber.i("Retrying..")
		return RetryException()
	}

	private fun isJPG(imgURL: String): Boolean {
		return EXTENSION_JPG in imgURL
	}

	private fun isPNG(imgURL: String): Boolean {
		return EXTENSION_PNG in imgURL
	}

	private fun isJPGOrPNG(imgURL: String): Boolean {
		return isJPG(imgURL) || isPNG(imgURL)
	}
}
