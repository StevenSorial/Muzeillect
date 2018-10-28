package com.steven.muzeillect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_settings)
		back.setOnClickListener {
			onBackPressed()
		}
	}

	override fun onResume() {
		super.onResume()
		back.visibility = if (isTaskRoot) View.GONE else View.VISIBLE
	}
}
