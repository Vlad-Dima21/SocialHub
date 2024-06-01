package com.vladima.socialhub.ui.main.settings

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import com.vladima.socialhub.R
import com.vladima.socialhub.databinding.FragmentSettingsBinding
import com.vladima.socialhub.ui.auth.AuthActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var binding: FragmentSettingsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        binding!!.toolbar.title = getString(R.string.settings)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel: SettingsFragmentViewModel by hiltNavGraphViewModels(R.id.nav_graph)

        lifecycleScope.launch {
            viewModel.user.collect { user ->
                if (user != null) {
                    binding!!.homeText.text = getString(R.string.logged_in_as, user.userName)
                }
            }
        }

        binding!!.logoutBtn.setOnClickListener {
            viewModel.signOut()
            activity?.startActivity(Intent(activity, AuthActivity::class.java))
            activity?.finish()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}