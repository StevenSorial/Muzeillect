package com.steven.muzeillect

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class PermissionRequestActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
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
