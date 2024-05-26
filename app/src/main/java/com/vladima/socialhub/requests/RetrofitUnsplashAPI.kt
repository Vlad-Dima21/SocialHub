package com.vladima.socialhub.requests

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://api.unsplash.com/"
object RetrofitUnsplashAPI {
    val api: UnsplashAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UnsplashAPI::class.java)
    }
}