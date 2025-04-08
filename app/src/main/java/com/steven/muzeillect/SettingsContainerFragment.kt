package com.steven.muzeillect

import androidx.compose.ui.graphics.ColorFilter
import android.view.View
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView

@Composable
fun SettingsContainerScreen() {

  Column(
    modifier = Modifier.fillMaxSize()
  ) {

    Image(
      painter = painterResource(R.drawable.logo_thick),
      contentDescription = "Logo",
      modifier = Modifier
        .align(Alignment.CenterHorizontally),
      colorFilter = ColorFilter.tint(colorResource(R.color.colorAccent))
    )

    Spacer(modifier = Modifier.weight(0.25f))
    val fragmentManager = findFragmentActivity()?.supportFragmentManager

    AndroidView(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      factory = {
        FragmentContainerView(it).apply {
          id = View.generateViewId()
          post {
            if (fragmentManager?.findFragmentById(id) == null) {
              fragmentManager?.beginTransaction()
                ?.replace(id, SettingsFragment())
                ?.commitNowAllowingStateLoss()
            }
          }
        }
      }
    )
  }
}

@Composable
fun findFragmentActivity(): FragmentActivity? {
  return LocalActivity.current as? FragmentActivity
}
