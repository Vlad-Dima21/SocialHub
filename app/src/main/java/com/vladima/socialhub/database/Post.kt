package com.vladima.socialhub.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Post(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val ownerId: String,
    val userName: String,
    val description: String,
    val imageUrl: String,
)
