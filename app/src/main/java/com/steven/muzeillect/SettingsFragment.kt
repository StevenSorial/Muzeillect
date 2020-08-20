package com.steven.muzeillect

import android.os.Bundle
import androidx.annotation.Keep
import androidx.recyclerview.widget.RecyclerView
import com.takisoft.preferencex.PreferenceFragmentCompat

@Keep
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
