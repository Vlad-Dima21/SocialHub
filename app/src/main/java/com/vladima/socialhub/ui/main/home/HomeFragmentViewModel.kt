package com.vladima.socialhub.ui.main.home

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.vladima.socialhub.models.FirestoreUserPost
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

@HiltViewModel
class HomeFragmentViewModel @Inject constructor(
    private val app: Application
): ViewModel(){
    private val currentUser = FirebaseAuth.getInstance().currentUser!!
    private val storageRef = FirebaseStorage.getInstance().reference.child(this.currentUser.uid)

    private var _userPosts = listOf<RVUserPost>()
    private val _filteredPosts = MutableStateFlow(listOf<RVUserPost>())
    val userPosts = _filteredPosts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val tempFiles = mutableListOf<File>()

    private val userPostsCollection = Firebase.firestore.collection("userPosts")

    init {
        loadCurrentUserPosts()
    }

    fun loadCurrentUserPosts() = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.emit(true)

        val firestoreUserPosts = userPostsCollection.whereEqualTo("userUID", currentUser.uid).get().await().documents.map {
            it.toObject(FirestoreUserPost::class.java)!!
        }

        val imageRefs = storageRef.listAll().await()
        val posts = mutableListOf<RVUserPost>()
        val jobs = mutableListOf<Job>()
        imageRefs.items.forEach{ storageReference ->
            jobs.add(
                launch(Dispatchers.IO){
                    val localFile = File.createTempFile(storageReference.name, "jpg")
                    tempFiles.add(localFile)
                    storageReference.getFile(localFile).await()
                    val fbBitmap = BitmapFactory.decodeFile(localFile.absolutePath)

                    // for some reason, high quality images lag the recycler view, so used memory needs to be reduced
                    val bitmap = Bitmap.createScaledBitmap(fbBitmap, fbBitmap.width / 2, fbBitmap.height / 2, false)
                    posts.add(RVUserPost(storageReference.name,
                        bitmap, firestoreUserPosts.find { it.fileName == storageReference.name }?.description ?: storageReference.name))
                }
            )
        }
        // fetch all user posts concurrently
        jobs.forEach { it.join() }
        _userPosts = posts.sortedByDescending { firestoreUserPosts.find { it2 -> it2.fileName == it.fileName }?.createDate }
        _filteredPosts.emit(_userPosts)
        _isLoading.emit(false)
        clearCache()
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

    private fun clearCache() = tempFiles.forEach {
            it.delete()
        }
}