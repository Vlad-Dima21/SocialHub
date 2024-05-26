package com.vladima.socialhub.models

import com.google.gson.annotations.SerializedName

data class UnsplashPost(
    @SerializedName("id") val id: String,
    @SerializedName("description") val description: String?,
    @SerializedName("alt_description") val altDescription: String?,
    @SerializedName("urls") val urls: Urls,
    @SerializedName("user") val user: UnsplashUser
)

data class Urls(
    @SerializedName("raw") val raw: String,
    @SerializedName("full") val full: String,
    @SerializedName("regular") val regular: String,
    @SerializedName("small") val small: String,
    @SerializedName("thumb") val thumb: String
)

data class UnsplashUser(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String?,
    @SerializedName("name") val name: String?,
)