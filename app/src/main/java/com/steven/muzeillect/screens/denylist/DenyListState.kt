package com.steven.muzeillect.screens.denylist

import android.net.Uri

data class DenyListState(
  val items: List<DenyListItemState> = emptyList(),
  val isRefreshing: Boolean = false,
)

sealed class DenyListItemState(val token: String) {
  class Loading(token: String) : DenyListItemState(token)
  class Success(token: String, val url: Uri) : DenyListItemState(token)
  class Error(token: String) : DenyListItemState(token)
}


