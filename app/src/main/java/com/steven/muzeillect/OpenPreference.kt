package com.steven.muzeillect

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference

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
