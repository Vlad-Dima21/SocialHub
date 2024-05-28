package com.vladima.socialhub.requests

enum class UnsplashTopic(val topicId: String, val topicName: String) {
    UGC("B02qM02HN9g", "User Generated"),
    Wallpapers("bo8jQKTaE0Y", "Wallpapers"),
    Nature("6sMVjTLSkeQ", "Nature"),
    Travel("Fzo3zuOHN6w", "Travel");

    companion object {
        fun getTopicById(id: String): UnsplashTopic {
            return entries.first { it.topicId == id }
        }

        fun getTopics() = entries.toList()
    }
}