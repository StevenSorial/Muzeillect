package com.steven.muzeillect

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
		val majorVersion = context?.packageManager?.getPackageInfo("net.nurik.roman.muzei", 0)
				?.versionName?.split(".")?.get(0)?.toIntOrNull() ?: 0
		if (majorVersion >= 3) {
			preferenceScreen.findPreference(context?.getString(R.string.pref_key_wifi)).isVisible = false
			preferenceScreen.findPreference(context?.getString(R.string.pref_key_interval)).isVisible = false
		}
		listView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
	}
}
