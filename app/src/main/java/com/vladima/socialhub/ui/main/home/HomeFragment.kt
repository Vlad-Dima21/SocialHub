package com.vladima.socialhub.ui.main.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.vladima.socialhub.R
import com.vladima.socialhub.databinding.FragmentHomeBinding
import com.vladima.socialhub.ui.components.PostCard
import com.vladima.socialhub.ui.components.PostRVAdapter
import com.vladima.socialhub.ui.helpers.BaseFragment
import com.vladima.socialhub.ui.helpers.MarginItemDecoration
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment() {

    private var binding: FragmentHomeBinding? = null
    private var posts = listOf<PostCard>()
    private val viewModel: HomeFragmentViewModel by hiltNavGraphViewModels(R.id.nav_graph)
    private lateinit var postsAdapter: PostRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.apply {
            getBoolean("home_refresh", false).let {
                if (it) {
                    lifecycleScope.launch {
                        viewModel.loadCurrentUserPosts()
                    }
                }
            }
            remove("home_refresh")
        }

        postsAdapter = PostRVAdapter(posts, viewModel::onFavorite)

        with(binding!!.rvPosts) {
            addItemDecoration(MarginItemDecoration(80))
            layoutManager = LinearLayoutManager(context)
            adapter = postsAdapter
        }

        repeatOnLifecycleStarted {
            viewModel.userPosts.collect { list ->
                posts = list
                postsAdapter.setNewPosts(posts)
            }
        }

        repeatOnLifecycleStarted {
            viewModel.isLoading.collect { isLoading ->
                if (isLoading) {
                    // keep the swipe refresh layout spinner on screen
                    binding?.apply {
                        swipeRefresh.isRefreshing = true
                    }
                } else {
                    binding?.apply {
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

        binding!!.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterPosts(newText)
                return true
            }
        })

        binding!!.addPost.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToNewPostFragment())
        }

        binding!!.swipeRefresh.setOnRefreshListener {
            binding!!.search.setQuery("", false)
            viewModel.loadCurrentUserPosts()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}