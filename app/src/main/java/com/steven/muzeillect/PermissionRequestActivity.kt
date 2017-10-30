package com.steven.muzeillect

import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager

class PermissionRequestActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
		}
	}

	override fun onStart() {
		super.onStart()
		val permission = intent.getStringExtra(KEY_PERMISSION)
		ActivityCompat.requestPermissions(this, arrayOf(permission), 0)
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		onBackPressed()
	}

	override fun onBackPressed() {
		super.onBackPressed()
		overridePendingTransition(0, 0)
	}
}
