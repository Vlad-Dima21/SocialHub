package com.vladima.socialhub.ui.components

import androidx.recyclerview.widget.DiffUtil

class PostsCallback(
    private val oldPosts: List<PostCard>,
    private val newPosts: List<PostCard>,
): DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldPosts.size

    override fun getNewListSize(): Int = newPosts.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = oldPosts[oldItemPosition].postId == newPosts[newItemPosition].postId

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = oldPosts[oldItemPosition] == newPosts[newItemPosition]
}