package com.steven.muzeillect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  private val appBarConfiguration by lazy {
    AppBarConfiguration(if (isTaskRoot) setOf(R.id.fragment_settings_container) else emptySet())
  }
  private val navController by lazy { findNavController(R.id.navHostFragment) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setSupportActionBar(toolbar)
    setupActionBarWithNavController(navController, appBarConfiguration)
    supportActionBar?.elevation = 0f
    supportActionBar?.setDisplayShowTitleEnabled(false)
  }

  override fun onSupportNavigateUp(): Boolean {
    return if (navController.currentDestination?.id == navController.graph.startDestination) {
      onBackPressed()
      true
    } else {
      navController.navigateUp(appBarConfiguration)
    }
  }
}
