package com.vladima.socialhub.ui.main.top_posts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vladima.socialhub.databinding.PostImageViewholderBinding
import com.vladima.socialhub.models.UnsplashPost

class TopPostsRVAdapter(
    private var posts: List<UnsplashPost>
): RecyclerView.Adapter<TopPostsRVAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: PostImageViewholderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = PostImageViewholderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun getItemCount(): Int = posts.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        with(holder) {
            with(posts[position]) {
                Glide.with(binding.root)
                    .load(urls.regular)
                    .into(binding.imageView)
                binding.authorName.text = user.name ?: user.username ?: "Unknown"
                binding.imageDescription.text = description ?: "No description"
            }
        }
    }

    fun setNewPosts(newList: List<UnsplashPost>) {
        posts = newList
        notifyDataSetChanged()
    }
}