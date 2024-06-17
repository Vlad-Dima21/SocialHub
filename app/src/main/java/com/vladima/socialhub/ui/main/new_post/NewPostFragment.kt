package com.vladima.socialhub.ui.main.new_post

import android.content.Intent
import android.os.Build
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.vladima.socialhub.R
import com.vladima.socialhub.databinding.FragmentNewPostBinding
import com.vladima.socialhub.ui.helpers.BaseFragment
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class NewPostFragment : BaseFragment() {

    private var binding: FragmentNewPostBinding? = null
    private val viewModel: NewPostViewModel by hiltNavGraphViewModels(R.id.nav_graph)
    private var isLoading = false
    private var cameraIntentFirstLaunch = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewPostBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraIntentFirstLaunch = savedInstanceState?.getBoolean("cameraIntentFirstLaunch", false) ?: false

        binding!!.newPostImage.setOnClickListener {
            if (!isLoading) {
                capturePhoto()
            }
        }

        binding!!.createPostBtn.setOnClickListener {
            viewModel.createPost(
                binding!!.descriptionEdt.text.toString(),
            )
        }

        repeatOnLifecycleStarted {
            viewModel.postCreated.collect { postCreated ->
                if (postCreated) {
                    findNavController().navigate(
                        R.id.action_newPostFragment_to_homeFragment,
                        Bundle().apply { putBoolean("home_refresh", true) },
                        NavOptions.Builder().setPopUpTo(R.id.homeFragment, true).build()
                    )
                }
            }
        }

        repeatOnLifecycleStarted {
            viewModel.helperMessage.collect { message ->
                binding!!.description.helperText = message ?: ""
            }
        }

        repeatOnLifecycleStarted {
            viewModel.isLoading.collect { isLoading ->
                this@NewPostFragment.isLoading = isLoading
                binding!!.description.isEnabled = !isLoading
                binding!!.createPostBtn.isEnabled = !isLoading
                binding!!.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        if (!cameraIntentFirstLaunch) {
            capturePhoto()
            cameraIntentFirstLaunch = true
        } else {
            binding!!.newPostImage.setImageURI(viewModel.imageFile!!.toUri())
        }

        findNavController().addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.newPostFragment) {
                viewModel.resetFields()
            }
        }
    }

    private fun capturePhoto() {
        val imageFile = viewModel.createFileForPhoto()
        getImageActivityResult.launch(
            Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    FileProvider.getUriForFile(requireContext(), requireActivity().applicationContext.packageName + ".provider", imageFile)
                )
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        )
    }

    private val getImageActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        with(result) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                viewModel.capturedPhoto = true
                binding!!.newPostImage.setImageURI(viewModel.imageFile!!.toUri())
            } else if (resultCode == AppCompatActivity.RESULT_CANCELED && !viewModel.capturedPhoto) {
                findNavController().popBackStack()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("cameraIntentFirstLaunch", cameraIntentFirstLaunch)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}