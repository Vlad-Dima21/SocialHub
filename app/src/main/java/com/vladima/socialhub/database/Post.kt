package com.vladima.socialhub.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.Date

@Entity
data class Post(
    @PrimaryKey
    val postId: String,
    val ownerId: String,
    val userName: String,
    val description: String,
    val imageUrl: String,
    val addedAt: Date = Date()
)
