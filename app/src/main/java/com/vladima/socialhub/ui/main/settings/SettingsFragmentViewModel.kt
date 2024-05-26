package com.vladima.socialhub.ui.main.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vladima.socialhub.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SettingsFragmentViewModel @Inject constructor(
    private val app: Application
) : ViewModel() {
    private val currentUser = FirebaseAuth.getInstance().currentUser!!

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() = viewModelScope.launch(Dispatchers.IO) {
        val usersCollection = Firebase.firestore.collection("users")
        val currentUser = usersCollection.whereEqualTo("userUID", currentUser.uid).get().await().documents[0].toObject(User::class.java)!!
        _user.emit(currentUser)
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }
}