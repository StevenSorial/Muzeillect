package com.steven.muzeillect

import android.os.Bundle
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {

	override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.preferences, rootKey)
	}

	override fun onStart() {
		super.onStart()
		setDivider(null)
	}
}
