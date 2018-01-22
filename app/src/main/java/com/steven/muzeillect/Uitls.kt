@file:JvmName("Utils")

package com.steven.muzeillect

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.muddzdev.styleabletoastlibrary.StyleableToast
import java.util.Random
import java.util.concurrent.ThreadLocalRandom

const val RETRY_INTERVAL = 5L

const val BASE_URL = "http://archillect.com/"

const val KEY_PERMISSION: String = "permission"

const val EXTENSION_JPG = ".jpg"
const val EXTENSION_PNG = ".png"

const val MINIMUM_WIDTH = 1000
const val MINIMUM_HEIGHT = 1000

private fun buildStyledToast(context: Context, message: String) {
	return StyleableToast.Builder(context)
			.length(Toast.LENGTH_SHORT)
			.text(message)
			.textColor(ContextCompat.getColor(context, R.color.colorAccent))
			.font(R.font.inconsolata)
			.backgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
			.show()
}

fun showToast(context: Context, message: String) {
	if (Looper.myLooper() == Looper.getMainLooper()) {
		buildStyledToast(context, message)
	} else {
		Handler(Looper.getMainLooper()).post {
			buildStyledToast(context, message)
		}
	}
}

fun isConnectedToWifi(context: Context): Boolean {
	val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
	val networkInfo = cm.activeNetworkInfo
	return (networkInfo != null
			&& networkInfo.isConnected
			&& networkInfo.type == ConnectivityManager.TYPE_WIFI)
}

fun isExternalStorageWritable() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

fun isPermissionGranted(context: Context, permission: String): Boolean {

	if (ContextCompat.checkSelfPermission(context, permission)
			== PackageManager.PERMISSION_GRANTED) return true

	val i = Intent(context, PermissionRequestActivity::class.java)
	i.putExtra(KEY_PERMISSION, permission)
	i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
	context.startActivity(i)
	return false
}

fun getArchillectLink(id: Long): String = BASE_URL + id

fun getRandomLong(bound: Long): Long {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
		return ThreadLocalRandom.current().nextLong(bound)
	} else {
		return Random().nextInt(bound.toInt()).toLong()
	}
}
