package com.steven.muzeillect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {

	override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.preferences, rootKey)
	}

	override fun onStart() {
		super.onStart()
		setDivider(null)
		listView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
	}

	override fun onResume() {
		super.onResume()
		val isMuzeiActive = try {
			context!!.packageManager.getApplicationInfo(MUZEI_PACKAGE_NAME, 0).enabled
		} catch (e: Exception) {
			false
		}
		disableSettings(isMuzeiActive)
		val isNewAPI = try {
			context!!.packageManager.getPackageInfo(MUZEI_PACKAGE_NAME, 0).versionName
					.split(".")[0].toInt()
		} catch (e: Exception) {
			0
		} >= 3

		hideLegacySettings(!isNewAPI)

		(findPreference(getString(R.string.pref_key_open)) as? OpenPreference)?.run {
			isVisible = if (activity?.isTaskRoot == true) {
				true
			} else {
				false
				return@run
			}

			title = if (isMuzeiActive) getString(R.string.pref_title_open) else getString(R.string.pref_title_install)
			summary = if (isMuzeiActive) getString(R.string.pref_desc_open, getString(R.string.app_name)) else getString(R.string.pref_desc_install)
			onClickBlock = {
				if (isMuzeiActive) {
					startActivity(context.packageManager.getLaunchIntentForPackage("net.nurik.roman.muzei"))
				} else {
					try {
						startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$MUZEI_PACKAGE_NAME")))
					} catch (e: Exception) {
						startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$MUZEI_PACKAGE_NAME")))
					}
				}
			}
		}
	}

	private fun hideLegacySettings(visible: Boolean) {
		findPreference(context?.getString(R.string.pref_key_wifi)).isVisible = visible
		findPreference(context?.getString(R.string.pref_key_interval)).isVisible = visible
	}

	private fun disableSettings(enabled: Boolean) {
		findPreference(context?.getString(R.string.pref_key_wifi)).isEnabled = enabled
		findPreference(context?.getString(R.string.pref_key_interval)).isEnabled = enabled
		findPreference(context?.getString(R.string.pref_key_hd)).isEnabled = enabled
	}
}
