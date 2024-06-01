package com.vladima.socialhub.ui.main.favorite_posts

import android.app.Application
import android.util.Log
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

    private val _deletedPosts = MutableStateFlow(mutableListOf<Post>())
    private val _favoritePosts = postsDao.getUserFavorites(currentUser.uid)

    val favoritePosts = combine(_favoritePosts, _deletedPosts) { posts, deletedPosts ->
        posts.filter { post -> deletedPosts.none { it.postId == post.postId } }
            .map { PostCard(it.postId, it.imageUrl, it.description, true, it.userName) }
    }
        .stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = emptyList())

    fun onMarkForRemoval(post: PostCard, isChecked: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val toBeDeleted = postsDao.getPostById(post.postId)
            _deletedPosts.emit(_deletedPosts.value.toMutableList().apply { add(toBeDeleted!!) })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onAddBackToFavorites(post: PostCard) = viewModelScope.launch(Dispatchers.IO) {
        _deletedPosts.emit(_deletedPosts.value.filter { it.postId != post.postId }.toMutableList())
    }

    fun onDeletePost(post: PostCard) = viewModelScope.launch(Dispatchers.IO) {
        val dbPost = postsDao.getPostById(post.postId)
        if (dbPost != null) {
            postsDao.deletePost(dbPost)
        }
    }
}