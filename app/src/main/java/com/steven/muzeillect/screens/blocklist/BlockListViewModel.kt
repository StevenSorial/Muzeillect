package com.steven.muzeillect.screens.blocklist

import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import com.steven.muzeillect.NetworkClient
import com.steven.muzeillect.utils.PrefsKey
import com.steven.muzeillect.utils.tokenUrlForToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.collections.indexOfFirst
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

class BlockListViewModel(
  private val dataStore: DataStore<Preferences>
) : ViewModel() {
  private val _uiState = MutableStateFlow(BlockListState())
  val uiState: StateFlow<BlockListState> = _uiState.asStateFlow()
  private var loadJob: Job? = null

  fun startsListening() {
    viewModelScope.launch {
      dataStore.data.collect { prefs ->
        handlePrefs(PrefsKey.BlockList.getFrom(prefs))
      }
    }
  }

  fun refresh() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isRefreshing = true)
      delay(0.5.seconds)
      val currentList = dataStore.data.map { PrefsKey.BlockList.getFrom(it) }.first()
      handlePrefs(currentList)
      _uiState.value = _uiState.value.copy(isRefreshing = false)
    }
  }

  private fun handlePrefs(currentList: Set<String>) {
    loadJob?.cancel()
    loadJob = viewModelScope.launch {
      withContext(Dispatchers.IO) {
        handlePrefsImpl(currentList)
      }
    }
  }

  private suspend fun handlePrefsImpl(currentSet: Set<String>) {
    val newBlockSet = currentSet.toMutableSet()

    for (token in currentSet) {

      val currentList = _uiState.value.items.toMutableList()
      var currentIndex = currentList.indexOfFirstOrNull(token)

      if (currentIndex != null && currentList[currentIndex] !is BlockListItemState.Error) continue

      currentIndex = currentIndex ?: currentList.size
      currentList.appendOrReplace(currentIndex, BlockListItemState.Loading(token))
      _uiState.value = _uiState.value.copy(items = currentList.toList())
      try {
        val url = NetworkClient.getImageUrl(tokenUrlForToken(token))
        val finalUrl = NetworkClient.getImageData(url) { response ->
          response.request.url.toString().toUri()
        }
        if (finalUrl.toString().contains("media_violation")) {
          newBlockSet.remove(token)
          currentList.removeAt(currentIndex)
        } else {
          currentList.appendOrReplace(currentIndex, BlockListItemState.Success(token, finalUrl))
        }
        _uiState.value = _uiState.value.copy(items = currentList.toList())
      } catch (e: Exception) {
        if (e !is CancellationException) {
          imageLoadingError(token, e)
        } else {
          currentList.removeAt(currentIndex)
          _uiState.value = _uiState.value.copy(items = currentList.toList())
          return
        }
      }
    }

    if (newBlockSet != currentSet) {
      dataStore.edit { PrefsKey.BlockList.setIn(it, newBlockSet.toSet()) }
    }
  }

  fun removeFromList(token: String) {
    viewModelScope.launch {
      val currentList = _uiState.value.items.toMutableList()
      currentList.removeAt(currentList.indexOfFirstOrNull(token)!!)
      _uiState.value = _uiState.value.copy(items = currentList.toList())
      val currentSet = dataStore.data.map { PrefsKey.BlockList.getFrom(it) }.first()
      val newBlockList = currentSet.toMutableSet()
      newBlockList.remove(token)
      dataStore.edit { PrefsKey.BlockList.setIn(it, newBlockList) }
    }
  }

  fun imageLoadingError(token: String, error: Throwable) {
    Timber.e(error, "Error downloading Image")
    val currentList = _uiState.value.items.toMutableList()
    val index = currentList.indexOfFirstOrNull(token) ?: return
    currentList[index] = BlockListItemState.Error(token)
    _uiState.value = _uiState.value.copy(items = currentList.toList())
  }
}

private fun <E> MutableList<E>.appendOrReplace(index: Int, element: E) {
  if (index < size) {
    this[index] = element
  } else if (index == size) {
    add(element)
  }
}

private fun List<BlockListItemState>.indexOfFirstOrNull(token: String): Int? {
  val index = indexOfFirst { it.token == token }
  return if (index == -1) null else index
}

