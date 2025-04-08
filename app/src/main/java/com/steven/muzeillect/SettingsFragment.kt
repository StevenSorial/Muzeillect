package com.steven.muzeillect

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView

class SettingsFragment : PreferenceFragmentCompat() {

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.preferences, rootKey)
  }

  override fun onStart() {
    super.onStart()
    setDivider(null)
    listView?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
  }
}
