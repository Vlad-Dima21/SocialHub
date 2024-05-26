package com.vladima.socialhub.ui.main.top_posts

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vladima.socialhub.models.UnsplashPost
import com.vladima.socialhub.requests.RetrofitUnsplashAPI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopPostsFragmentViewModel @Inject constructor(
    private val app: Application
): ViewModel() {
    private var _topPostsList = listOf<UnsplashPost>()
    private val _topPosts = MutableStateFlow(_topPostsList)
    val topPosts = _topPosts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadTopPosts()
    }

    fun loadTopPosts() = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.emit(true)

        RetrofitUnsplashAPI.api.getTopPosts().let { response ->
            if (response.isSuccessful) {
                response.body()?.let { posts ->
                    _topPostsList = posts
                    _topPosts.emit(posts)
                }
            }
        }

        _isLoading.emit(false)
    }
}