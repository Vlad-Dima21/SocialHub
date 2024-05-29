package com.vladima.socialhub.models

import java.util.Date

data class FirestoreUserPost(
    var postId: String = "",
    var fileName: String = "",
    var userUID: String = "",
    var description: String = "",
    var createDate: Date = Date()
)