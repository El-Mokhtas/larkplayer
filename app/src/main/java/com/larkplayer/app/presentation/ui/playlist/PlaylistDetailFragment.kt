package com.larkplayer.app.presentation.ui.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.larkplayer.app.R
import com.larkplayer.app.databinding.FragmentPlaylistDetailBinding
import com.larkplayer.app.presentation.adapter.MediaItemAdapter
import com.larkplayer.app.presentation.viewmodel.PlaylistViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaylistDetailFragment : Fragment(R.layout.fragment_playlist_detail) {

    private var _binding: FragmentPlaylistDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaylistViewModel by viewModels()
    private val args: PlaylistDetailArgs by lazy {
        PlaylistDetailArgs(arguments?.getLong("playlistId", 0L) ?: 0L)
    }

    private data class PlaylistDetailArgs(val playlistId: Long)

    private val adapter = MediaItemAdapter(
        onItemClick = { media ->
            // Play from this position
        },
        onItemLongClick = { media ->
            // Enable drag to reorder
        },
        onMoreClick = { media ->
            // Show options
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupDragToReorder()
        observePlaylist()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PlaylistDetailFragment.adapter
        }
    }

    private fun setupDragToReorder() {
        val callback = object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder): Int {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                return makeMovementFlags(dragFlags, 0)
            }

            override fun onMove(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, target: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                // Handle reorder
                return true
            }

            override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {}

            override fun isLongPressDragEnabled(): Boolean = true
        }
        ItemTouchHelper(callback).attachToRecyclerView(binding.recyclerView)
    }

    private fun observePlaylist() {
        viewModel.getPlaylistItemsPaging(args.playlistId)
            .flow
            .cachedIn(lifecycleScope)
            .collect { pagingData ->
                adapter.submitData(lifecycleScope.coroutineContext, pagingData)
            }

        viewModel.getPlaylistByIdUseCase(args.playlistId)?.let { playlist ->
            binding.tvPlaylistName.text = playlist.name
            binding.tvTrackCount.text = "${playlist.itemCount} tracks"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}