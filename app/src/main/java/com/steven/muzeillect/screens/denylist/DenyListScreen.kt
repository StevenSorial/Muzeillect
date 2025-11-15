package com.steven.muzeillect.screens.denylist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.steven.muzeillect.NetworkClient
import com.steven.muzeillect.uiComponents.AppBar
import com.steven.muzeillect.utils.settingsDataStore

@Composable
fun DenyListScreen(
  vm: DenyListViewModel = DenyListViewModel(LocalContext.current.settingsDataStore)
) {

  val uiState by vm.uiState.collectAsState()
  vm.startsListening()

  Scaffold(
    topBar = { AppBar(title = { Text("Denied List") }) }
  ) { padding ->

    BoxWithConstraints(
      modifier = Modifier
        .padding(padding)
        .consumeWindowInsets(padding)
    ) {
      val midDim = minOf(maxWidth, maxHeight)
      val size = maxOf(midDim * 0.4f, 192.dp)
      val count = (maxWidth / size).toInt().coerceAtLeast(1)


      PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { vm.refresh() },
      ) {
        LazyVerticalGrid(
          columns = GridCells.Fixed(count),
          modifier = Modifier
            .fillMaxSize(),
          contentPadding = PaddingValues(0.dp),
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
  vm: DenyListViewModel,
  itemState: DenyListItemState
) {
  var showMenu by remember { mutableStateOf(false) }

  when (itemState) {
    is DenyListItemState.Loading -> LoadingItem()
    is DenyListItemState.Error -> Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Text("⚠\uFE0F")
    }

    is DenyListItemState.Success ->
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
      text = { Text("Remove") },
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
    println("maxWidth: $maxWidth, maxHeight: $maxHeight")
    val minDim = minOf(maxWidth, maxHeight)
    val size = minDim * 0.25f

    CircularProgressIndicator(modifier = Modifier.size(size), strokeWidth = 6.0.dp)
  }
}

