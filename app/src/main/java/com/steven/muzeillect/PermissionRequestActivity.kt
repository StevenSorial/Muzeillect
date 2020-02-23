package com.steven.muzeillect

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class PermissionRequestActivity : AppCompatActivity() {

  private val permission: String by lazy { intent.getStringExtra(KEY_PERMISSION) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    if (isPermissionGranted(permission)) finish()
  }

  override fun onStart() {
    super.onStart()
    ActivityCompat.requestPermissions(this, arrayOf(permission), 0)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    finish()
  }
}
