package com.steven.muzeillect

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.net.toUri
import com.google.android.apps.muzei.api.MuzeiContract.Sources
import com.steven.muzeillect.utils.MUZEI_PACKAGE_NAME
import com.steven.muzeillect.utils.showToast

class MuzeillectRedirectActivity : ComponentActivity() {

  private val activityLauncher = registerForActivityResult(StartActivityForResult()) { finish() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val isProviderSelected = Sources.isProviderSelected(this, BuildConfig.APPLICATION_ID)
    val deepLinkIntent = Sources.createChooseProviderIntent(BuildConfig.APPLICATION_ID)
    val appName = getString(R.string.app_name)

    val enableMessage = if (isProviderSelected) null else getString(
      R.string.toast_enable, appName
    )
    if (tryStartIntent(deepLinkIntent, enableMessage)) {
      return
    }
    val enableSourceMessage =
      if (isProviderSelected) null else getString(
        R.string.toast_enable_source,
        appName
      )
    val launchIntent = packageManager.getLaunchIntentForPackage(MUZEI_PACKAGE_NAME)
    if (launchIntent != null && tryStartIntent(launchIntent, enableSourceMessage)) {
      return
    }
    val playStoreIntent =
      Intent(Intent.ACTION_VIEW).setData("https://play.google.com/store/apps/details?id=${MUZEI_PACKAGE_NAME}".toUri())
    if (tryStartIntent(playStoreIntent, getString(R.string.toast_muzei_missing_error))) {
      return
    }
    showToast(getString(R.string.toast_play_store_missing_error))
    finish()
  }

  private fun tryStartIntent(intent: Intent, message: String?): Boolean {
    try {
      activityLauncher.launch(intent)
      message?.let { showToast(it) }
      return true
    } catch (_: Exception) {
      return false
    }
  }
}
