package com.steven.muzeillect

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.preference.PreferenceManager
import android.util.Log
import com.google.android.apps.muzei.api.*
import org.jsoup.Jsoup
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.concurrent.thread

class ArchillectArtSource : RemoteMuzeiArtSource("ArchillectArtSource") {

	private val TAG = ArchillectArtSource::class.java.simpleName

	private val COMMAND_ID_SHARE = 111
	private val COMMAND_ID_SAVE = 222

	private var pref: SharedPreferences? = null
	private var updateInterval: Long? = null
	private var isOnWiFiOnly: Boolean? = null

	override fun onCreate() {
		super.onCreate()
		Log.d(TAG, "Service Created")

		setUserCommands(
				UserCommand(MuzeiArtSource.BUILTIN_COMMAND_ID_NEXT_ARTWORK),
				UserCommand(COMMAND_ID_SAVE, getString(R.string.action_save)),
				UserCommand(COMMAND_ID_SHARE, getString(R.string.action_share))
		)

		pref = PreferenceManager.getDefaultSharedPreferences(this)
		updateInterval = pref!!.getString(getString(R.string.pref_key_interval), getString(R
				.string.pref_interval_value_default)).toLong()
		isOnWiFiOnly = pref!!.getBoolean(getString(R.string.pref_key_wifi), false)
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

		Log.d(TAG, "Trying to update")

		if (isOnWiFiOnly!! && !isConnectedWifi(this)) {
			Log.d(TAG, "Update on wifi only..Rescheduling")
			throw RetryException()
		}

		val oldToken: String = currentArtwork?.token ?: "-1"
		Log.d(TAG, "old token = $oldToken")

		val newToken: Int = getRandomToken()
		val newImgUrl: String = getImageURL(newToken)

		if (Integer.parseInt(oldToken) == newToken || !isJPGOrPNG(newImgUrl)) {
			throw RetryException()
		}
		Log.d(TAG, "New Image Token: $newToken")
		Log.d(TAG, "New Image URL: $newImgUrl")

		checkIfUrlExist(newImgUrl)

		publishArtwork(Artwork.Builder()
				.title(newToken.toString())
				.byline("Archillect")
				.imageUri(Uri.parse(newImgUrl))
				.token(newToken.toString())
				.viewIntent(Intent(Intent.ACTION_VIEW,
						Uri.parse(BASE_URL + newToken)))
				.build())
		scheduleUpdate(System.currentTimeMillis() + (updateInterval!! * MINUTE_MILLIS))

	}

	@Throws(RemoteMuzeiArtSource.RetryException::class)
	private fun getRandomToken(): Int {
		try {
			Log.d(TAG, "Generating Image Token")
			val doc = Jsoup.connect(BASE_URL).get()
			val element = doc.select("div.overlay").first()
			val lastToken = Integer.parseInt(element.text())
			val randToken = Random().nextInt(lastToken + 1)
			Log.d(TAG, "Generated Image Token: " + randToken)
			return randToken
		} catch (e: Exception) {
			Log.e(TAG, "Error generating Token", e)
			throw RetryException()
		}
	}

	@Throws(RemoteMuzeiArtSource.RetryException::class)
	private fun getImageURL(token: Int): String {
		try {
			Log.d(TAG, "Generating Image Token")
			val doc = Jsoup.connect(createArchillectLink(token.toString())).get()
			val img = doc.select("#ii").first()
			val imgUrl = img.attr("src")
			Log.d(TAG, "Generated Image URL: $imgUrl")
			return imgUrl
		} catch (e: Exception) {
			Log.e(TAG, "Error generating Image URL", e)
			throw RetryException()
		}
	}

	private fun isJPGOrPNG(imgURL: String): Boolean {
		return isJPG(imgURL) || isPNG(imgURL)
	}

	private fun isJPG(imgURL: String): Boolean {
		return EXTENSION_JPG in imgURL
	}

	private fun isPNG(imgURL: String): Boolean {
		return EXTENSION_PNG in imgURL
	}

	@Throws(RemoteMuzeiArtSource.RetryException::class)
	private fun checkIfUrlExist(URLString: String) {
		try {
			val connection = URL(URLString).openConnection() as HttpURLConnection
			connection.connect()
			Log.d(TAG, "response code: " + connection.responseCode)
		} catch (e: IOException) {
			Log.e(TAG, "Error trying connecting to url", e)
			throw RetryException()
		}
	}

	private fun saveImage() {
		if (currentArtwork == null) {
			Log.d(TAG, "no artwork available")
			return
		}
		if (!isExternalStorageWritable()) {
			Log.d(TAG, "Storage is Not Writable")
			return
		}
		if (!isPermissionGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) return

		thread(start = true) {
			Log.d(TAG, "Saving Image")
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
					.DIRECTORY_PICTURES).toString())
			if (!folder.exists()) {
				Log.d(TAG, "creating Pictures Folder")
				folder.mkdirs()
			}
			val file = File(folder, currentArtwork.token + ext)
			if (file.exists()) {
				Log.d(TAG, "File already exists")
				showToast(this, getString(R.string.message_save_exists))
				return@thread
			}
			try {
				os = FileOutputStream(file)
				val saved = bitmap.compress(format, 100, os)
				os.flush()
				os.close()
				if (saved) {
					Log.d(TAG, "Saving Finished")
					showToast(this, getString(R.string.message_save_complete))
					MediaScannerConnection.scanFile(this,
							arrayOf(file.toString()), null
					) { path, uri ->
						Log.d(TAG, "Scanned: $path")
						Log.d(TAG, "-> uri: $uri")
					}
				} else {
					Log.d(TAG, "Saving Error")
					showToast(this, getString(R.string.message_save_error))
				}
			} catch (e: FileNotFoundException) {
				Log.e(TAG, "Saving Error", e)
				showToast(this, getString(R.string.message_save_error))
			} catch (e: IOException) {
				Log.e(TAG, "Saving Error", e)
				showToast(this, getString(R.string.message_save_error))
			}
		}
	}

	private fun shareImage() {
		if (currentArtwork == null) return
		if (currentArtwork.token == null) return
		val i = Intent()
		i.action = Intent.ACTION_SEND
		i.putExtra(Intent.EXTRA_TEXT, createArchillectLink(currentArtwork.token))
		i.type = "text/plain"
		startActivity(Intent.createChooser(i, getString(R.string.action_share)).setFlags(Intent
				.FLAG_ACTIVITY_NEW_TASK))
	}
}
