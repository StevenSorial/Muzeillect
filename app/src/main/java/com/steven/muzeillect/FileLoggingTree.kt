package com.steven.muzeillect

import android.app.Application
import android.content.Context
import android.util.Log
import timber.log.Timber
import java.util.Date
import java.util.concurrent.TimeUnit.DAYS

class FileLoggingTree(private val application: Application) : Timber.DebugTree() {

	override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {

		val logTimeStamp = Date()
		val log = "$logTimeStamp ${getPriorityString(priority)}/$tag: $message\n"
		val fileName = "log.txt"
		val fileAccessMode = Context.MODE_PRIVATE or Context.MODE_APPEND
		val file = application.getFileStreamPath(fileName)

		if (file.lastModified() + DAYS.toMillis(30) < System.currentTimeMillis()) {
			file.delete()
			Log.d("tag", "log deleted")
		}
		val lastModified = file.lastModified()
		try {
			val fileStream = application.openFileOutput(fileName, fileAccessMode)
			fileStream.write(log.toByteArray())
			fileStream.close()
		} catch (e: Exception) {
			Log.e("tag", "Error while logging into file : " + e)
		}
		if (lastModified != 0L) {
			file.setLastModified(lastModified)
		}
	}

	private fun getPriorityString(priority: Int): String = when (priority) {
		Log.VERBOSE -> "Verbose"
		Log.DEBUG -> "Debug"
		Log.INFO -> "Info"
		Log.WARN -> "Warn"
		Log.ERROR -> "Error"
		Log.ASSERT -> "Assert"
		else -> ""
	}
}
