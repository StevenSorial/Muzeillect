package com.steven.muzeillect

import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.android.apps.muzei.api.Artwork
import com.google.android.apps.muzei.api.MuzeiArtSource
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource
import org.jsoup.Jsoup
import java.io.IOException
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class ArchillectArtSource : RemoteMuzeiArtSource("ArchillectArtSource") {

	private val TAG = ArchillectArtSource::class.java.simpleName

	private val SECOND_MILLIS = 1000
	private val MINUTE_MILLIS = 60 * SECOND_MILLIS
	private val HOUR_MILLIS = 60 * MINUTE_MILLIS
	private val ROTATE_TIME_MILLIS = 3 * HOUR_MILLIS // rotate every 3 hours

	private val BASE_URL = "http://archillect.com/"

	override fun onCreate() {
		super.onCreate()
		setUserCommands(MuzeiArtSource.BUILTIN_COMMAND_ID_NEXT_ARTWORK)
	}

	override fun onTryUpdate(p0: Int) {
		val oldToken: String = currentArtwork?.token ?: "-1"
		Log.d(TAG, "old token = " + oldToken)

		var newToken: Int = getRandomToken()
		var newImgUrl: String = getImageURL(newToken)

		while (Integer.parseInt(oldToken) == newToken
				|| !isJPGOrPNG(newImgUrl)) {
			newToken = getRandomToken()
			newImgUrl = getImageURL(newToken)
		}
		Log.d(TAG, "New Image Token = " + newToken)
		Log.d(TAG, "New Image URL = " + newImgUrl)

		checkifUrlExist(newImgUrl)

		publishArtwork(Artwork.Builder()
				.title(newToken.toString())
				.imageUri(Uri.parse(newImgUrl))
				.token(newToken.toString())
				.viewIntent(Intent(Intent.ACTION_VIEW,
						Uri.parse(BASE_URL + newToken)))
				.build())
		scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS)
	}

	private fun getRandomToken(): Int {
		try {
			Log.d(TAG, "Generating Image Token")
			val doc = Jsoup.connect(BASE_URL).get()
			val element = doc.select("div.overlay").first()
			val lastToken = Integer.parseInt(element.text())
			val randToken = Random().nextInt(lastToken + 1)
			Log.d(TAG, "Generated Image Token = " + randToken)
			return randToken
		} catch (e: Exception) {
			Log.e(TAG, "Error generating Token", e)
			throw RetryException()
		}
	}

	private fun getImageURL(token: Int): String {
		try {
			Log.d(TAG, "Generating Image Token")
			val doc = Jsoup.connect(BASE_URL + token).get()
			val img = doc.select("#ii").first()
			val imgUrl = img.attr("src")
			Log.d(TAG, "Generated Image URL = " + imgUrl)
			return imgUrl
		} catch (e: Exception) {
			Log.e(TAG, "Error generating Image URL", e)
			throw RetryException()
		}
	}

	private fun isJPGOrPNG(imgURL: String): Boolean {
		return ".jpg" in imgURL || ".png" in imgURL
	}

	private fun checkifUrlExist(URLString: String) {
		try {
			val connection = URL(URLString).openConnection() as HttpURLConnection
			connection.connect()
			Log.d(TAG, "response code" + connection.responseCode)
		} catch (e: IOException) {
			Log.e(TAG, "Error trying connecting to url", e)
			throw RetryException()
		}
	}

}
