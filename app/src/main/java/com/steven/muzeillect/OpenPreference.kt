package com.steven.muzeillect

import android.content.Context
import android.support.v7.preference.Preference
import android.util.AttributeSet

class OpenPreference : Preference {

	var onClickBlock: (Preference) -> (Unit) = {}

	constructor(context: Context)
			: super(context)

	constructor(context: Context, attrs: AttributeSet?)
			: super(context, attrs)

	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
			: super(context, attrs, defStyleAttr)

	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
			: super(context, attrs, defStyleAttr, defStyleRes)

	override fun onClick() {
		super.onClick()
		onClickBlock(this)
	}
}
