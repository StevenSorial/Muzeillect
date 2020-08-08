package com.steven.muzeillect

import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Bundle
import androidx.core.net.toUri
import androidx.preference.Preference
import androidx.recyclerview.widget.RecyclerView
import com.takisoft.preferencex.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {

  override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.preferences, rootKey)
  }

  override fun onStart() {
    super.onStart()
    setDivider(null)
    listView?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
  }

  override fun onResume() {
    super.onResume()
    val isMuzeiActive = try {
      context?.packageManager?.getApplicationInfo(MUZEI_PACKAGE_NAME, 0)?.enabled == true
    } catch (e: NameNotFoundException) {
      false
    }
    disableSettings(isMuzeiActive)

    findPreference<OpenPreference>(getString(R.string.pref_key_open))?.run {
      isVisible = activity?.isTaskRoot == true
      if (activity?.isTaskRoot != true) return@run

      title = if (isMuzeiActive) getString(R.string.pref_title_open) else getString(R.string.pref_title_install)
      summary = if (isMuzeiActive) getString(R.string.pref_desc_open, getString(R.string.app_name)) else getString(R.string.pref_desc_install)
      onClickBlock = {
        if (isMuzeiActive) {
          startActivity(context.packageManager.getLaunchIntentForPackage("net.nurik.roman.muzei"))
        } else {
          try {
            startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$MUZEI_PACKAGE_NAME".toUri()))
          } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$MUZEI_PACKAGE_NAME".toUri()))
          }
        }
      }
    }
  }

  private fun disableSettings(enabled: Boolean) {
    val context = context ?: return
    findPreference<Preference>(context.getString(R.string.pref_key_hd))?.isEnabled = enabled
  }
}
