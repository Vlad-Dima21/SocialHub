package com.vladima.socialhub.requests

import com.vladima.socialhub.models.UnsplashPost
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

private const val CLIENT = "k2i9Phy00ONYt7fCFRwHjgNEGPUA9LNAFzhlitySQQw"

interface UnsplashAPI {
    @GET("photos")
    @Headers("Accept-Version: v1")
    suspend fun getTopPosts(
        @Query("client_id") clientId: String = CLIENT,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<List<UnsplashPost>>

    @GET("photos/random")
    @Headers("Accept-Version: v1")
    suspend fun getPostsByTopic(
        @Query("topics") topics: String? = null,
        @Query("count") count: Int = 20,
        @Query("client_id") clientId: String = CLIENT,
    ): Response<List<UnsplashPost>>
}