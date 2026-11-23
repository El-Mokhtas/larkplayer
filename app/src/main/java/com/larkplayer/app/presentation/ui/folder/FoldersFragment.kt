package com.larkplayer.app.presentation.ui.folder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.larkplayer.app.R
import com.larkplayer.app.databinding.FragmentFoldersBinding
import com.larkplayer.app.presentation.adapter.FolderAdapter
import com.larkplayer.app.presentation.viewmodel.FolderViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FoldersFragment : Fragment(R.layout.fragment_folders) {

    private var _binding: FragmentFoldersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FolderViewModel by viewModels()

    private val adapter = FolderAdapter(
        onItemClick = { folder ->
            viewModel.onFolderClick(folder)
        },
        onItemLongClick = { folder ->
            // Show options
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoldersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeFolders()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = this@FoldersFragment.adapter
        }
    }

    private fun observeFolders() {
        viewModel.folders.collect { folders ->
            adapter.submitData(lifecycleScope.coroutineContext, folders)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}