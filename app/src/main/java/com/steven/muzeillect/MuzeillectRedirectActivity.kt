package com.steven.muzeillect

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.StringRes
import androidx.core.net.toUri
import com.google.android.apps.muzei.api.MuzeiContract.Sources

class MuzeillectRedirectActivity : ComponentActivity() {

  private val activityLauncher = registerForActivityResult(StartActivityForResult()) { finish() }

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
    showToast(R.string.toast_play_store_missing_error)
    finish()
  }

  private fun tryStartIntent(intent: Intent, @StringRes toastResId: Int?): Boolean {
    try {
      activityLauncher.launch(intent)
      toastResId?.let { showToast(it) }
      return true
    } catch (e: Exception) {
      return false
    }
  }
}
