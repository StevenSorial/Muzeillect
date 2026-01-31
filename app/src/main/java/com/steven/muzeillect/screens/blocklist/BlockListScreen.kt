package com.steven.muzeillect.screens.blocklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import com.steven.muzeillect.NetworkClient
import com.steven.muzeillect.R
import com.steven.muzeillect.uiComponents.MyAppBar
import com.steven.muzeillect.utils.copyWith

@Composable
fun BlockListScreen(
  vm: BlockListViewModel = hiltViewModel()
) {

  val uiState by vm.uiState.collectAsStateWithLifecycle()
  vm.startsListening()

  Scaffold(
    topBar = {
      MyAppBar(title = { Text(stringResource(R.string.action_blocked_images)) })
    }
  ) { padding ->

    @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
    BoxWithConstraints(
      modifier = Modifier
        .padding(padding.copyWith(bottom = 0.dp)),
    ) {
      val maxHeight = this.maxHeight
      val midDim = minOf(this.maxWidth, maxHeight)
      val size = maxOf(midDim * 0.4f, 180.dp)
      val count = (this.maxWidth / size).toInt().coerceAtLeast(1)

      PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,

        onRefresh = { vm.refresh() },
      ) {
        LazyVerticalGrid(
          columns = GridCells.Fixed(count),
          modifier = Modifier
            .fillMaxSize(),
          contentPadding = PaddingValues(bottom = padding.calculateBottomPadding() + (maxHeight / 6)),
          horizontalArrangement = Arrangement.spacedBy(0.dp),
          verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

          items(
            uiState.items,
            key = { it.token }
          ) {
            Box(
              modifier = Modifier
                .aspectRatio(1f)
                .animateItem(),
              contentAlignment = Alignment.Center
            ) {
              GridItem(vm, it)
            }
          }
        }
      }
    }
  }
}

@Suppress("AssignedValueIsNeverRead")
@Composable
fun GridItem(
  vm: BlockListViewModel,
  itemState: BlockListItemState
) {
  var showMenu by remember { mutableStateOf(false) }

  when (itemState) {
    is BlockListItemState.Loading -> LoadingItem()
    is BlockListItemState.Error -> Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Text("⚠\uFE0F")
    }

    is BlockListItemState.Success ->
      SubcomposeAsyncImage(
        model = itemState.url,
        imageLoader = NetworkClient.imageLoader,
        contentDescription = null,
        loading = { LoadingItem() },
        onError = { vm.imageLoadingError(itemState.token, it.result.throwable) },
        modifier = Modifier
          .aspectRatio(1f)
          .fillMaxSize()
          .clickable { showMenu = true },
        contentScale = ContentScale.Crop
      )
  }

  DropdownMenu(
    expanded = showMenu,
    onDismissRequest = { showMenu = false },
  ) {
    DropdownMenuItem(
      text = { Text(stringResource(R.string.action_blocked_image_unblock)) },
      onClick = {
        showMenu = false
        vm.removeFromList(itemState.token)
      }
    )
  }
}

@Composable
private fun LoadingItem() {
  BoxWithConstraints(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    val minDim = minOf(this.maxWidth, this.maxHeight)
    val size = minDim * 0.25f

    @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
    CircularProgressIndicator(modifier = Modifier.size(size), strokeWidth = 6.0.dp)
  }
}

