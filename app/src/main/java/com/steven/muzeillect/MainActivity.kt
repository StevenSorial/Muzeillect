package com.steven.muzeillect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.steven.muzeillect.databinding.MainActivityBinding

class MainActivity : AppCompatActivity() {

  private lateinit var binding: MainActivityBinding

  private val navController by lazy {
    supportFragmentManager.findFragmentById(binding.navHostFragment.id)!!.findNavController()
  }

  private val appBarConfiguration by lazy {
    AppBarConfiguration(if (isTaskRoot) setOf(R.id.fragment_settings_container) else emptySet())
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = MainActivityBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setSupportActionBar(binding.toolbar)
    setupActionBarWithNavController(navController, appBarConfiguration)
    supportActionBar!!.elevation = 0f
    supportActionBar!!.setDisplayShowTitleEnabled(false)
  }

  override fun onSupportNavigateUp(): Boolean {
    if (navController.currentDestination?.id == navController.graph.startDestination) {
      onBackPressed()
      return true
    } else {
      return navController.navigateUp(appBarConfiguration)
    }
  }
}
