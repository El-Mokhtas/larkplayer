package com.larkplayer.app.presentation.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.larkplayer.app.R
import com.larkplayer.app.databinding.FragmentQueueBinding
import com.larkplayer.app.presentation.adapter.MediaItemAdapter
import com.larkplayer.app.presentation.viewmodel.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QueueFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentQueueBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayerViewModel by viewModels()

    private val adapter = MediaItemAdapter(
        onItemClick = { media ->
            viewModel.setCurrentMedia(media, viewModel.queue.value, viewModel.queue.value.indexOf(media))
            dismiss()
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
        _binding = FragmentQueueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeQueue()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = this@QueueFragment.adapter
        }
    }

    private fun observeQueue() {
        viewModel.queue.collect { queue ->
            adapter.submitData(lifecycleScope.coroutineContext, queue)
            binding.tvCount.text = "${queue.size} tracks"
            binding.emptyView.visibility = if (queue.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (queue.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}