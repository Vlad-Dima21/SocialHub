package com.vladima.socialhub.ui.main.home

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.vladima.socialhub.database.AppDatabase
import com.vladima.socialhub.database.Post
import com.vladima.socialhub.models.FirestoreUserPost
import com.vladima.socialhub.models.User
import com.vladima.socialhub.ui.components.PostCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HomeFragmentViewModel @Inject constructor(
    private val app: Application
): ViewModel(){
    private val currentUser = FirebaseAuth.getInstance().currentUser!!
    private val storageRef = FirebaseStorage.getInstance().reference.child(this.currentUser.uid)
    private val postsDao by lazy { AppDatabase.getDatabase(app).postDao() }

    private var _userPosts = listOf<RVUserPost>()
    private val _filteredPosts = MutableStateFlow(listOf<RVUserPost>())
    val userPosts = _filteredPosts.asStateFlow().combine(postsDao.getAllPosts()) { uPosts: List<RVUserPost>, dbPosts: List<Post> ->
        uPosts.map { uPost ->
            val isFavorite = dbPosts.any { it.postId == uPost.postId }
            PostCard(
                uPost.postId,
                uPost.imageUrl,
                uPost.imageDescription,
                isFavorite
            )
        }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val usersCollection = Firebase.firestore.collection("users")
    private val userPostsCollection = Firebase.firestore.collection("userPosts")


    init {
        loadCurrentUserPosts()
    }

    fun loadCurrentUserPosts() = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.emit(true)

        val firestoreUserPosts = userPostsCollection.whereEqualTo("userUID", currentUser.uid).get().await().documents.map {
            it.toObject(FirestoreUserPost::class.java)!!.apply { postId = it.id }
        }

        val imageRefs = storageRef.listAll().await()
        val posts = mutableListOf<RVUserPost>()
        val jobs = mutableListOf<Job>()
        imageRefs.items.forEach{ storageReference ->
            jobs.add(
                launch(Dispatchers.IO){
                    val post = firestoreUserPosts.find { it.fileName == storageReference.name }
                    val imageUrl = storageReference.downloadUrl.await().toString()
                    posts.add(RVUserPost(post?.postId ?: "", storageReference.name,
                        imageUrl, post?.description ?: storageReference.name))
                }
            )
        }
        // fetch all user posts concurrently
        jobs.forEach { it.join() }
        _userPosts = posts.sortedByDescending { firestoreUserPosts.find { it2 -> it2.fileName == it.fileName }?.createDate }
        _filteredPosts.emit(_userPosts)
        _isLoading.emit(false)
    }

    private var filterJob: Job? = null

    //debounced filtering
    fun filterPosts(query: String?) {
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            if (!query.isNullOrEmpty()) {
                delay(500)
            }
            _filteredPosts.emit(
                _userPosts.filter {
                    it.imageDescription.contains(query?.trim() ?: "", ignoreCase = true)
                }
            )
        }
    }

    fun onFavorite(post: PostCard, isChecked: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val completeUser = usersCollection.whereEqualTo("userUID", currentUser.uid).get()
                .await().documents.first().toObject(User::class.java)!!
            val userPost =
                postsDao.getPostById(post.postId) ?: Post(post.postId, currentUser.uid, completeUser.userName, post.imageDescription, post.imageUrl)
            when (isChecked) {
                true -> postsDao.insertPost(userPost)
                false -> postsDao.deletePost(userPost)
            }
        } catch (e: Exception) {
            Toast.makeText(app, "Error has occurred", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}