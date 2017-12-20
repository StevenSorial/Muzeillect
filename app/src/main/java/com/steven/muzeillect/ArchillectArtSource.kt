package com.steven.muzeillect

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
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
import org.jsoup.Jsoup
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit.MINUTES
import kotlin.concurrent.thread

class ArchillectArtSource : RemoteMuzeiArtSource("ArchillectArtSource") {

	private companion object {
		private const val COMMAND_ID_SHARE = 111
		private const val COMMAND_ID_SAVE = 222
	}

	private lateinit var pref: SharedPreferences
	private var updateInterval = 0L
	private var isOnWiFiOnly = false
	private var isHDOnly = false

	override fun onCreate() {
		super.onCreate()

		Timber.d("Service Created")

		setUserCommands(
				UserCommand(MuzeiArtSource.BUILTIN_COMMAND_ID_NEXT_ARTWORK),
				UserCommand(COMMAND_ID_SAVE, getString(R.string.action_save)),
				UserCommand(COMMAND_ID_SHARE, getString(R.string.action_share))
		)

		pref = PreferenceManager.getDefaultSharedPreferences(this)
		updateInterval = pref.getString(getString(R.string.pref_key_interval), getString(R
				.string.pref_interval_value_default)).toLong()
		isOnWiFiOnly = pref.getBoolean(getString(R.string.pref_key_wifi), false)
		isHDOnly = pref.getBoolean(getString(R.string.pref_key_hd), false)
	}

	override fun onCustomCommand(id: Int) {
		super.onCustomCommand(id)
		when (id) {
			COMMAND_ID_SAVE -> saveImage()
			COMMAND_ID_SHARE -> shareImage()
		}
	}

	@Throws(RemoteMuzeiArtSource.RetryException::class)
	override fun onTryUpdate(p0: Int) {

		Timber.d("Trying to update")

		if (isOnWiFiOnly && !isConnectedWifi(this)) {
			Timber.d("Update on wifi only..Rescheduling")
			scheduleUpdate(System.currentTimeMillis() + MINUTES.toMillis(RETRY_INTERVAL))
			return
		}

		val oldToken: String = currentArtwork?.token ?: "-1"
		Timber.d("old token = $oldToken")

		val newToken: Int = getRandomToken()
		val newImgUrl: String = getImageURL(newToken)

		if (oldToken.toInt() == newToken || !isJPGOrPNG(newImgUrl)) {
			Timber.d("Invalid Format..Retrying")
			throw RetryException()
		}

		checkImageSize(newImgUrl)

		publishArtwork(Artwork.Builder()
				.title(newToken.toString())
				.byline("Archillect")
				.imageUri(Uri.parse(newImgUrl))
				.token(newToken.toString())
				.viewIntent(Intent(Intent.ACTION_VIEW,
						Uri.parse(BASE_URL + newToken)))
				.build())
		scheduleUpdate(System.currentTimeMillis() + MINUTES.toMillis(updateInterval))
	}

	@Throws(RemoteMuzeiArtSource.RetryException::class)
	private fun getRandomToken(): Int {
		try {
			Timber.d("Generating Image Token")
			val doc = Jsoup.connect(BASE_URL).get()
			val element = doc.select("div.overlay").first()
			val lastToken = element.text().toInt()
			val randToken = getRandomInt(lastToken) + 1
			Timber.d("Generated Image Token: $randToken")
			return randToken
		} catch (e: Exception) {
			Timber.e(e, "Error generating Token")
			throw RetryException()
		}
	}

	@Throws(RemoteMuzeiArtSource.RetryException::class)
	private fun getImageURL(token: Int): String {
		try {
			Timber.d("Generating Image Token")
			val doc = Jsoup.connect(createArchillectLink(token.toString())).get()
			val img = doc.select("#ii").first()
			val imgUrl = img.attr("src")
			Timber.d("Generated Image URL: $imgUrl")
			return imgUrl
		} catch (e: Exception) {
			Timber.e(e, "Error generating Image URL")
			throw RetryException()
		}
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

	@Throws(RemoteMuzeiArtSource.RetryException::class)
	private fun checkImageSize(URLString: String) {
		try {
			val connection = URL(URLString).openConnection() as HttpURLConnection
			val bitmap = BitmapFactory.decodeStream(connection.inputStream)
			val h = bitmap.height
			val w = bitmap.width
			Timber.d("Image Resolution: $w x $h")
			Timber.d("Device Resolution: ${getDisplaySize(this)}")

			if (isHDOnly && (h < MINIMUM_HEIGHT || w < MINIMUM_WIDTH)) {
				Timber.d("Resolution is low..Retrying")
				throw RetryException()
			}
			Timber.d("response code: ${connection.responseCode}")
		} catch (e: IOException) {
			Timber.e(e, "Error trying connecting to url..Retrying")
			throw RetryException()
		}
	}

	private fun saveImage() {
		if (currentArtwork?.token == null) {
			Timber.d("No artwork available")
			return
		}
		if (!isExternalStorageWritable()) {
			Timber.d("Storage is Not Writable")
			return
		}
		if (!isPermissionGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) return

		thread {
			Timber.d("Saving Image")
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
				Timber.d("creating directory")
				folder.mkdirs()
			}
			val file = File(folder, currentArtwork.token + ext)
			if (file.exists()) {
				Timber.d("File already exists")
				showToast(this, getString(R.string.message_save_exists))
				return@thread
			}
			try {
				os = FileOutputStream(file)
				val saved = bitmap.compress(format, 100, os)
				os.flush()
				os.close()
				if (saved) {
					Timber.d("Saving Finished")
					showToast(this, getString(R.string.message_save_complete))
					MediaScannerConnection.scanFile(this,
							arrayOf(file.toString()), null
					) { path, uri ->
						Timber.d("Scanned: $path")
						Timber.d("-> uri: $uri")
					}
				} else {
					Timber.d("Saving Error")
					showToast(this, getString(R.string.message_save_error))
				}
			} catch (e: FileNotFoundException) {
				Timber.e(e, "Saving Error")
				showToast(this, getString(R.string.message_save_error))
			} catch (e: IOException) {
				Timber.e(e, "Saving Error")
				showToast(this, getString(R.string.message_save_error))
			}
		}
	}

	private fun shareImage() {
		if (currentArtwork?.token == null) return
		val i = Intent()
		i.action = Intent.ACTION_SEND
		i.putExtra(Intent.EXTRA_TEXT, createArchillectLink(currentArtwork.token))
		i.type = "text/plain"
		startActivity(Intent.createChooser(i, getString(R.string.action_share)).setFlags(Intent
				.FLAG_ACTIVITY_NEW_TASK))
	}
}
