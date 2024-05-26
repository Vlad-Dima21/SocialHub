package com.vladima.socialhub.ui.main.top_posts

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.vladima.socialhub.R
import com.vladima.socialhub.databinding.FragmentTopPostsBinding
import com.vladima.socialhub.models.UnsplashPost
import com.vladima.socialhub.ui.helpers.MarginItemDecoration
import kotlinx.coroutines.launch

class TopPostsFragment : Fragment() {

    private var binding: FragmentTopPostsBinding? = null
    private var posts = listOf<UnsplashPost>()
    private var postsAdapter = TopPostsRVAdapter(listOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTopPostsBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).supportActionBar?.hide()

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel: TopPostsFragmentViewModel by hiltNavGraphViewModels(R.id.nav_graph)

        with(binding!!.rvPosts) {
            addItemDecoration(MarginItemDecoration(80))
            layoutManager = LinearLayoutManager(context)
            adapter = postsAdapter
        }

        lifecycleScope.launch {
            viewModel.topPosts.collect { list ->
                posts = list
                postsAdapter.setNewPosts(posts)
            }
        }


        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                if (isLoading) {
                    // keep the swipe refresh layout spinner on screen
                    with(binding!!) {
                        swipeRefresh.isRefreshing = true
                    }
                } else {
                    with(binding!!) {
                        swipeRefresh.isRefreshing = false
                        if (posts.isEmpty()) {
                            noPosts.visibility = View.VISIBLE
                        } else {
                            noPosts.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }
}