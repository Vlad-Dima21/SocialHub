package com.vladima.socialhub.ui.components

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vladima.socialhub.databinding.PostCardViewholderBinding

class PostRVAdapter(
    private var posts: List<PostCard>,
    private var onFavorite: (PostCard, Boolean) -> Unit
): RecyclerView.Adapter<PostRVAdapter.PostViewHolder>() {

    inner class PostViewHolder(val binding: PostCardViewholderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int = posts.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostCardViewholderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        with(holder) {
            with(posts[position]) {
                Glide.with(binding.root)
                    .load(imageUrl)
                    .into(binding.imageView)
                binding.authorName.text = imageAuthorName
                if (imageAuthorName.isNullOrEmpty()) {
                    binding.authorName.visibility = android.view.View.GONE
                }
                binding.imageDescription.text = imageDescription
                binding.favoriteToggle.setOnCheckedListener {
                    onFavorite(this, it)
                }
                binding.favoriteToggle.isChecked = isFavorite
            }
        }
    }

    fun setNewPosts(newList: List<PostCard>) {
        posts = newList
        notifyDataSetChanged()
    }
}

data class PostCard(
    val postId: String,
    val imageUrl: String,
    val imageDescription: String,
    val isFavorite: Boolean,
    val imageAuthorName: String? = null,
)