package com.steven.muzeillect

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageButton

class SettingsActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_settings)
		findViewById<ImageButton>(R.id.back).setOnClickListener {
			finish()
		}
	}
}
