package com.vladima.socialhub.ui.main.favorite_posts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.vladima.socialhub.R
import com.vladima.socialhub.databinding.FragmentFavoritePostsBinding
import com.vladima.socialhub.ui.components.PostCard
import com.vladima.socialhub.ui.components.PostRVAdapter
import com.vladima.socialhub.ui.helpers.BaseFragment
import com.vladima.socialhub.ui.helpers.MarginItemDecoration

class FavoritePostsFragment : BaseFragment() {
    private lateinit var binding: FragmentFavoritePostsBinding
    private val viewModel: FavoritePostsViewModel by hiltNavGraphViewModels(R.id.nav_graph)
    private var posts = listOf<PostCard>()
    private lateinit var postsAdapter: PostRVAdapter
    private var deleteSnackbar: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoritePostsBinding.inflate(inflater, container, false)
        binding.toolbar.title = getString(R.string.favorite_posts)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postsAdapter = PostRVAdapter(posts) { postCard, _ ->
            deleteSnackbar?.dismiss()
            viewModel.onMarkForRemoval(postCard)
            Snackbar.make(
                binding.rvFavoritePosts,
                getString(R.string.post_removed_snackbar), Snackbar.LENGTH_SHORT
            )
                .setAction(getString(R.string.undo)
                ) { viewModel.onAddBackToFavorites(postCard) }
                .addCallback(object : Snackbar.Callback() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        if (event != DISMISS_EVENT_ACTION) {
                            viewModel.onDeletePost(postCard)
                        }
                    }
                }).let {
                    deleteSnackbar = it
                    it.show()
                }
        }

        with(binding.rvFavoritePosts) {
            addItemDecoration(MarginItemDecoration(80))
            layoutManager = LinearLayoutManager(context)
            adapter = postsAdapter
        }
        repeatOnLifecycleStarted {
            viewModel.favoritePosts.collect { posts ->
                this@FavoritePostsFragment.posts = posts
                postsAdapter.setNewPosts(posts)
                binding.noFavoritePosts.visibility = if(posts.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
}