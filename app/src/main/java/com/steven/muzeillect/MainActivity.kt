package com.steven.muzeillect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setSupportActionBar(toolbar)
    toolbar.setupWithNavController(findNavController(R.id.navHostFragment))
    supportActionBar?.elevation = 0f
    supportActionBar?.setDisplayShowTitleEnabled(false)
  }

  override fun onSupportNavigateUp() = findNavController(R.id.navHostFragment).navigateUp()
}
