package com.steven.muzeillect

import android.os.Bundle
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
}
