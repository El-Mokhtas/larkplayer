package com.larkplayer.app.presentation.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.larkplayer.app.R
import com.larkplayer.app.databinding.FragmentSongsBinding
import com.larkplayer.app.presentation.adapter.MediaItemAdapter
import com.larkplayer.app.presentation.viewmodel.LibraryViewModel
import com.larkplayer.app.presentation.viewmodel.LibraryTab
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SongsFragment : Fragment(R.layout.fragment_songs) {

    private var _binding: FragmentSongsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LibraryViewModel by viewModels()

    private val adapter = MediaItemAdapter { mediaItem ->
        playMedia(mediaItem)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeData()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SongsFragment.adapter
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.allAudio
                .distinctUntilChanged()
                .collect { items ->
                    adapter.submitData(lifecycleScope.coroutineContext, items)
                }
        }

        lifecycleScope.launch {
            viewModel.isScanning
                .distinctUntilChanged()
                .collect { isScanning ->
                    binding.progressBar.visibility = if (isScanning) View.VISIBLE else View.GONE
                    binding.emptyView.visibility = if (isScanning) View.GONE else View.VISIBLE
                }
        }
    }

    private fun playMedia(mediaItem: com.larkplayer.app.data.model.MediaItem) {
        val action = SongsFragmentDirections.actionSongsFragmentToPlayerFragment(mediaItem.id)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}