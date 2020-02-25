package com.steven.muzeillect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.steven.muzeillect.databinding.SettingsContainerFragmentBinding

class SettingsContainerFragment : Fragment() {

  private lateinit var binding: SettingsContainerFragmentBinding

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    binding = SettingsContainerFragmentBinding.inflate(inflater, container, false)
    return binding.root
  }
}
