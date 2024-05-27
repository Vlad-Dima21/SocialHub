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
import com.vladima.socialhub.ui.helpers.MarginItemDecoration
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null
    private var posts = listOf<RVUserPost>()
    private var postsAdapter = HomeRVAdapter(listOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).supportActionBar?.hide()

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel: HomeFragmentViewModel by hiltNavGraphViewModels(R.id.nav_graph)

        with(binding!!.rvPosts) {
            addItemDecoration(MarginItemDecoration(80))
            layoutManager = LinearLayoutManager(context)
            adapter = postsAdapter
        }

        lifecycleScope.launch {
            viewModel.userPosts.collect { list ->
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