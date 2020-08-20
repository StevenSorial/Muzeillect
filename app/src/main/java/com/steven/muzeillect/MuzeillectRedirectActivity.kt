package com.steven.muzeillect

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.core.net.toUri
import com.google.android.apps.muzei.api.MuzeiContract.Sources

class MuzeillectRedirectActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val isProviderSelected = Sources.isProviderSelected(this, "com.steven.muzeillect")
    val deepLinkIntent = Sources.createChooseProviderIntent("com.steven.muzeillect")
    val enableMessage = if (isProviderSelected) null else R.string.toast_enable
    if (tryStartIntent(deepLinkIntent, enableMessage)) {
      return
    }
    val enableSourceMessage = if (isProviderSelected) null else R.string.toast_enable_source
    val launchIntent = packageManager.getLaunchIntentForPackage(MUZEI_PACKAGE_NAME)
    if (launchIntent != null && tryStartIntent(launchIntent, enableSourceMessage)) {
      return
    }
    val playStoreIntent = Intent(Intent.ACTION_VIEW).setData("https://play.google.com/store/apps/details?id=$MUZEI_PACKAGE_NAME".toUri())
    if (tryStartIntent(playStoreIntent, R.string.toast_muzei_missing_error)) {
      return
    }
    showToast(R.string.toast_play_store_missing_error, Toast.LENGTH_LONG)
    finish()
  }

  private fun tryStartIntent(intent: Intent, @StringRes toastResId: Int?): Boolean {
    try {
      startActivityForResult(intent, 1)
      toastResId?.let { showToast(it, Toast.LENGTH_LONG) }
      return true
    } catch (e: Exception) {
      return false
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    finish()
  }
}
