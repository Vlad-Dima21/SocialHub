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
import androidx.navigation.fragment.findNavController
import com.vladima.socialhub.R
import com.vladima.socialhub.databinding.FragmentNewPostBinding
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class NewPostFragment : Fragment() {

    private var binding: FragmentNewPostBinding? = null
    private lateinit var viewModel: NewPostViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewPostBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).supportActionBar?.hide()
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel =  hiltNavGraphViewModels<NewPostViewModel>(R.id.nav_graph).value

        binding!!.newPostImage.setOnClickListener {
            capturePhoto()
        }

        binding!!.createPostBtn.setOnClickListener {
            viewModel.createPost(
                binding!!.descriptionEdt.text.toString(),
            )
        }

        lifecycleScope.launch {
            viewModel.postCreated.collect { postCreated ->
                if (postCreated) {
                    popBackStack()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.helperMessage.collect { message ->
                binding!!.description.helperText = message ?: ""
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding!!.description.isEnabled = !isLoading
                binding!!.createPostBtn.isEnabled = !isLoading
                binding!!.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        if (viewModel.capturedPhoto.value) {
            binding!!.newPostImage.setImageURI(viewModel.imageFile!!.toUri())
        } else {
            capturePhoto()
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
                viewModel.markAsCaptured()
                binding!!.newPostImage.setImageURI(viewModel.imageFile!!.toUri())
            } else if (resultCode == AppCompatActivity.RESULT_CANCELED && !viewModel.capturedPhoto.value) {
                popBackStack()
            }
        }
    }

    private fun popBackStack() {
        viewModel.clearFields()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearFields()
        binding = null
    }
}