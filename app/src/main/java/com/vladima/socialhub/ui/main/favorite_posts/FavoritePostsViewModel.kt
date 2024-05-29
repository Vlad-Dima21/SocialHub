package com.vladima.socialhub.ui.main.favorite_posts

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.vladima.socialhub.database.AppDatabase
import com.vladima.socialhub.database.Post
import com.vladima.socialhub.ui.components.PostCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritePostsViewModel @Inject constructor(
    private val app: Application
) : ViewModel() {
    private val currentUser = FirebaseAuth.getInstance().currentUser!!
    private val postsDao by lazy { AppDatabase.getDatabase(app).postDao() }

    private val _postMarkedToBeDeleted = MutableStateFlow<Post?>(null)
    val postMarkedToBeDeleted = _postMarkedToBeDeleted.asStateFlow()

    private val _favoritePosts = postsDao.getUserFavorites(currentUser.uid)
    val favoritePosts = _favoritePosts.combine(postMarkedToBeDeleted) { posts, postMarkedToBeDeleted ->
        posts.filter { it != postMarkedToBeDeleted }
            .map { PostCard(it.postId, it.imageUrl, it.description, true, it.userName) }
    }
        .stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = emptyList())

    fun onMarkForRemoval(post: PostCard, isChecked: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val toBeDeleted = postsDao.getPostById(post.postId)
            _postMarkedToBeDeleted.emit(toBeDeleted)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onAddBackToFavorites(post: Post) = viewModelScope.launch(Dispatchers.IO) {
        _postMarkedToBeDeleted.emit(null)
    }

    fun onDeletePost(post: Post) = viewModelScope.launch(Dispatchers.IO) {
        postsDao.deletePost(post)
        _postMarkedToBeDeleted.value = null
    }
}