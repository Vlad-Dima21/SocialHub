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

    @Query("SELECT * FROM post WHERE id = :id")
    fun getPostById(id: Int): Flow<Post>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    fun insertPost(post: Post)

    @Delete
    fun deletePost(post: Post)
}