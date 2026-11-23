package com.larkplayer.app.presentation.ui.folder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.larkplayer.app.R
import com.larkplayer.app.databinding.FragmentFolderDetailBinding
import com.larkplayer.app.presentation.adapter.MediaItemAdapter
import com.larkplayer.app.presentation.viewmodel.FolderViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FolderDetailFragment : Fragment(R.layout.fragment_folder_detail) {

    private var _binding: FragmentFolderDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FolderViewModel by viewModels()
    private val args: FolderDetailFragmentArgs by navArgs()

    private val adapter = MediaItemAdapter(
        onItemClick = { media ->
            // Play from this position
        },
        onItemLongClick = { media ->
            // Show options
        },
        onMoreClick = { media ->
            // Show more options
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFolderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeFolder()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = this@FolderDetailFragment.adapter
        }
    }

    private fun observeFolder() {
        viewModel.folderContents.collect { items ->
            adapter.submitData(lifecycleScope.coroutineContext, items)
        }

        viewModel.currentFolder.collect { folder ->
            folder?.let {
                binding.tvFolderName.text = it.displayName
                binding.tvFileCount.text = "${it.mediaCount} files"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}