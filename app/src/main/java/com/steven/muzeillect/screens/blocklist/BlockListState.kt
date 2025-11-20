package com.steven.muzeillect.screens.blocklist

import android.net.Uri

data class BlockListState(
  val items: List<BlockListItemState> = emptyList(),
  val isRefreshing: Boolean = false,
)

sealed class BlockListItemState(val token: String) {
  class Loading(token: String) : BlockListItemState(token)
  class Success(token: String, val url: Uri) : BlockListItemState(token)
  class Error(token: String) : BlockListItemState(token)
}


