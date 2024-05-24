package com.vladima.socialhub.models

data class User(
    var userUID: String = "",
    var userName: String = "",
    var age: Int = 0,
    var friends: List<String> = listOf()
)