package com.vladima.socialhub.ui.main.top_posts

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.vladima.socialhub.R
import com.vladima.socialhub.database.AppDatabase
import com.vladima.socialhub.database.Post
import com.vladima.socialhub.models.UnsplashPost
import com.vladima.socialhub.requests.RetrofitUnsplashAPI
import com.vladima.socialhub.requests.UnsplashTopic
import com.vladima.socialhub.ui.components.PostCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopPostsFragmentViewModel @Inject constructor(
    private val app: Application
): ViewModel() {

    private val currentUser = FirebaseAuth.getInstance().currentUser!!
    private val postsDao by lazy { AppDatabase.getDatabase(app).postDao() }

    private var _topPostsList = listOf<UnsplashPost>()
    private val _topPosts = MutableStateFlow(_topPostsList)

    val topPosts = _topPosts.asStateFlow().combine(postsDao.getAllPosts()) { uPosts: List<UnsplashPost>, dbPosts: List<Post> ->
        uPosts.map { uPost ->
            val isFavorite = dbPosts.any { it.postId == uPost.id }
            PostCard(
                uPost.id,
                uPost.urls.regular,
                uPost.description ?: uPost.altDescription ?: app.getString(R.string.no_description),
                isFavorite,
                uPost.user.name
            )
        }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val sharedPreferences = app.getSharedPreferences("SocialHub.TopPosts", Context.MODE_PRIVATE)

    val topics = UnsplashTopic.getTopics()
    private val _selectedTopics = HashSet<UnsplashTopic>(
        sharedPreferences.getString("selectedTopics", null)?.let { stringTopics ->
            if (stringTopics.isEmpty()) return@let null
            else {
                stringTopics.split(",").map { UnsplashTopic.getTopicById(it) }.toHashSet()
            }
        }
        ?: HashSet()
    )
    val selectedTopics get() = _selectedTopics.toSet()
    fun toggleTopic(topic: UnsplashTopic) {
        val editor = sharedPreferences.edit()

        if (_selectedTopics.contains(topic)) {
            _selectedTopics.remove(topic)
        } else {
            _selectedTopics.add(topic)
        }

        editor.putString("selectedTopics", _selectedTopics.joinToString(",", transform = { it.topicId }))
        editor.apply()
    }

    init {

        loadTopPosts()
    }

    private var unsplashPage: Int = 0

    fun loadTopPosts(loadMore: Boolean = false) = viewModelScope.launch(Dispatchers.IO) {

        if (loadMore && _selectedTopics.isNotEmpty()) {
            return@launch
        }

        _isLoading.emit(true)
        if (!loadMore) {
            unsplashPage = 0
        }

        val fetchedPosts = mutableListOf<UnsplashPost>()
        when(_selectedTopics.isNotEmpty()) {
            true -> {
                unsplashPage = 0
                RetrofitUnsplashAPI.api.getPostsByTopic(_selectedTopics.joinToString(",") { it.topicId })
            }
            else -> {
                unsplashPage++
                if (unsplashPage > 1) {
                    fetchedPosts.addAll(_topPostsList)
                }
                RetrofitUnsplashAPI.api.getTopPosts(unsplashPage)
            }
        }.let { response ->
            if (response.isSuccessful) {
                response.body()?.let { posts ->
                    fetchedPosts.addAll(posts)
                    _topPostsList = fetchedPosts
                    _topPosts.emit(fetchedPosts)
                }
            } else {
                Log.e("TopPostsFragmentViewModel", "Error loading top posts: ${response.errorBody()}")
                _topPostsList = listOf()
                _topPosts.emit(listOf())
            }
        }

        _isLoading.emit(false)
    }


    fun onFavorite(post: PostCard, isChecked: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val userPost = postsDao.getPostById(post.postId) ?: Post(
                post.postId,
                currentUser.uid,
                post.imageAuthorName ?: app.getString(R.string.unknown),
                post.imageDescription,
                post.imageUrl
            )
            when(isChecked) {
                true -> postsDao.insertPost(userPost)
                false -> postsDao.deletePost(userPost)
            }
        } catch (e: Exception) {
            Toast.makeText(app, "Error has occurred", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}