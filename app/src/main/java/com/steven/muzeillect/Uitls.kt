@file:JvmName("Utils")

package com.steven.muzeillect

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.widget.Toast
import com.muddzdev.styleabletoastlibrary.StyleableToast

const val SECOND_MILLIS: Long = 1000
const val MINUTE_MILLIS: Long = 60 * SECOND_MILLIS
// const val HOUR_MILLIS: Long = 60 * MINUTE_MILLIS

const val BASE_URL = "http://archillect.com/"

const val KEY_PERMISSION: String = "permission"

const val EXTENSION_JPG = ".jpg"
const val EXTENSION_PNG = ".png"

private fun buildStyledToast(context: Context, message: String) {
	return StyleableToast.Builder(context)
			.duration(Toast.LENGTH_SHORT)
			.text(message)
			.textColor(ContextCompat.getColor(context, R.color.colorAccent))
			.typeface(ResourcesCompat.getFont(context, R.font.inconsolata))
			.backgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
			.build()
			.show()
}

fun showToast(context: Context, message: String) {
	if (Looper.myLooper() == Looper.getMainLooper()) {
		buildStyledToast(context, message)
		return
	}
	Handler(Looper.getMainLooper()).post {
		buildStyledToast(context, message)
	}
}

fun isConnectedWifi(context: Context): Boolean {
	val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
	val networkInfo = cm.activeNetworkInfo
	return (networkInfo != null
			&& networkInfo.isConnected
			&& networkInfo.type == ConnectivityManager.TYPE_WIFI)
}

fun isExternalStorageWritable(): Boolean {
	return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
}

fun isPermissionGranted(context: Context, permission: String): Boolean {

	if (ContextCompat.checkSelfPermission(context, permission)
			== PackageManager.PERMISSION_GRANTED) return true

	val i = Intent(context, PermissionRequestActivity::class.java)
	i.putExtra(KEY_PERMISSION, permission)
	i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
	context.startActivity(i)
	return false
}