package com.vladima.socialhub.ui.main.top_posts

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.vladima.socialhub.R
import com.vladima.socialhub.databinding.FragmentTopPostsBinding
import com.vladima.socialhub.models.UnsplashPost
import com.vladima.socialhub.ui.components.PostCard
import com.vladima.socialhub.ui.components.PostRVAdapter
import com.vladima.socialhub.ui.helpers.BaseFragment
import com.vladima.socialhub.ui.helpers.MarginItemDecoration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class TopPostsFragment: BaseFragment() {

    private var binding: FragmentTopPostsBinding? = null
    private var posts = listOf<PostCard>()
    private lateinit var postsAdapter: PostRVAdapter

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
        postsAdapter = PostRVAdapter(posts, viewModel::onFavorite)

        binding!!.swipeRefresh.setOnRefreshListener {
            viewModel.loadTopPosts()
        }

        (binding?.topicsLayout)?.let {
            viewModel.topics.forEachIndexed { index, topic ->
                val button = Button(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(
                            context.resources.getDimension(R.dimen.card_spacing_small).toInt(),
                        )
                        marginStart = context.resources.getDimension(R.dimen.card_spacing_small).toInt()
                        if (index == viewModel.topics.size - 1) {
                            marginEnd = context.resources.getDimension(R.dimen.card_spacing_small).toInt()
                        }
                        setPadding(context.resources.getDimension(R.dimen.card_spacing_small).toInt())
                    }
                    background = AppCompatResources.getDrawable(context, R.drawable.btn_topic_selector)
                    setTextColor(AppCompatResources.getColorStateList(context, R.color.txt_selector))
                    isSelected = viewModel.selectedTopics.contains(topic)
                    isAllCaps = false
                }
                button.text = topic.topicName
                button.tag = topic.topicId
                button.setOnClickListener {
                    viewModel.toggleTopic(topic)
                    button.isSelected = !button.isSelected
                    viewModel.loadTopPosts()
                }
                it.addView(button)
            }
        }

        with(binding!!.rvPosts) {
            addItemDecoration(MarginItemDecoration(80))
            layoutManager = LinearLayoutManager(context)
            adapter = postsAdapter
        }

        repeatOnLifecycleStarted {
            viewModel.topPosts.collect { list ->
                posts = list
                postsAdapter.setNewPosts(posts)
                with(binding!!) {
                    if (posts.isEmpty()) {
                        noPosts.visibility = View.VISIBLE
                    } else {
                        noPosts.visibility = View.GONE
                    }
                }
            }
        }

        repeatOnLifecycleStarted {
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