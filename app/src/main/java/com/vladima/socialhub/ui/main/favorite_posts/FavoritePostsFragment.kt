package com.vladima.socialhub.ui.main.favorite_posts

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.vladima.socialhub.R
import com.vladima.socialhub.databinding.FragmentFavoritePostsBinding

class FavoritePostsFragment : Fragment() {
    private lateinit var binding: FragmentFavoritePostsBinding
    private val viewModel: FavoritePostsViewModel by hiltNavGraphViewModels(R.id.nav_graph)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoritePostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }
}