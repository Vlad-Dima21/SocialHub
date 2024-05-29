package com.vladima.socialhub.ui.main.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vladima.socialhub.databinding.HomeImageViewholderBinding

class HomeRVAdapter(
    private var posts: List<RVUserPost>,
    private var onFavorite: (RVUserPost, Boolean) -> Unit
): RecyclerView.Adapter<HomeRVAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: HomeImageViewholderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = HomeImageViewholderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun getItemCount(): Int = posts.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        with(holder) {
            with(posts[position]) {
                Glide.with(binding.root)
                    .load(imageUrl)
                    .into(binding.imageView)
                binding.imageDescription.text = imageDescription

                binding.favoriteToggle.setOnCheckedListener {
                    onFavorite(this, it)
                }
            }
        }
    }

    fun setNewPosts(newList: List<RVUserPost>) {
        posts = newList
        notifyDataSetChanged()
    }
}