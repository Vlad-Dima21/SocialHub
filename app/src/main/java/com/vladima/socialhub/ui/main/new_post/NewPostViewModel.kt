package com.vladima.socialhub.ui.main.new_post

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.vladima.socialhub.R
import com.vladima.socialhub.models.FirestoreUserPost
import com.vladima.socialhub.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class NewPostViewModel @Inject constructor(
    private val app: Application
) : ViewModel() {

    private val JPEG_QUALITY = 80

    private val currentUser = FirebaseAuth.getInstance().currentUser!!
    private val userPostCollection = Firebase.firestore.collection("userPosts")

    var imageFile: File? = null
    var capturedPhoto = false

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss")
    val storageRef: (String) -> StorageReference = { dateString ->
        FirebaseStorage.getInstance().reference.child("${currentUser.uid}/$dateString")
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _helperMessage = MutableStateFlow<String?>(null)
    val helperMessage = _helperMessage.asStateFlow()

    private val _postCreated = MutableStateFlow(false)
    val postCreated = _postCreated.asStateFlow()

    fun createPost(description: String?) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.value = true
        if (description.isNullOrEmpty()) {
            _isLoading.emit(false)
            _helperMessage.emit(app.getString(R.string.please_enter_a_description_first))
            return@launch
        } else {
            _helperMessage.emit(null)
            val compressedImage = compressFileAndGetUri(imageFile!!)
            val fileName = LocalDateTime.now().format(dateFormatter)
            storageRef(fileName).putFile(compressedImage)
                .addOnSuccessListener {
                    userPostCollection.add(
                        FirestoreUserPost(
                            fileName = fileName,
                            userUID = currentUser.uid,
                            description =  description
                        )
                    )
                    imageFile!!.delete()
                    Toast.makeText(app, app.getString(R.string.post_success), Toast.LENGTH_SHORT).show()
                    _isLoading.value = false
                    _postCreated.value = true
                    resetFields()
                }
                .addOnFailureListener {
                    Log.e("NewPostViewModel", "Error uploading image: ${it.message}")
                    Toast.makeText(app, app.getString(R.string.post_error), Toast.LENGTH_SHORT).show()
                    _isLoading.value = false
                }
        }
    }

    fun createFileForPhoto() = File(app.filesDir, LocalDateTime.now().format(dateFormatter)).also {
        it.createNewFile()
        imageFile = it
    }

    private fun compressFileAndGetUri(file: File): Uri {
        var bitmap = BitmapFactory.decodeFile(file.path)

        // Images are rotated -90f degrees when uploaded to Firebase Storage
        bitmap = Utils.rotateImage(bitmap, 90f)

        val byteStream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, byteStream)
        val byteArray = byteStream.toByteArray()

        val outputFile = FileOutputStream(file)
        with(outputFile) {
            write(byteArray)
            flush()
            close()
        }

        return Uri.fromFile(file)
    }

    private fun resetFields() {
        imageFile?.delete()
        imageFile = null
        capturedPhoto = false
        _helperMessage.value = null
        _postCreated.value = false
        _isLoading.value = false
    }

    override fun onCleared() {
        super.onCleared()
        resetFields()
    }
}