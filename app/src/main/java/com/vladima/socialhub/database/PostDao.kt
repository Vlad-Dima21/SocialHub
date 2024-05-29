package com.vladima.socialhub.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {

    @Query("SELECT * FROM post")
    fun getAllPosts(): Flow<List<Post>>

    @Query("SELECT * FROM post WHERE ownerId = :userUID")
    fun getUserFavorites(userUID: String): Flow<List<Post>>

    @Query("SELECT * FROM post WHERE postId = :id")
    suspend fun getPostById(id: String): Post?

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    @Delete
    suspend fun deletePost(post: Post)
}