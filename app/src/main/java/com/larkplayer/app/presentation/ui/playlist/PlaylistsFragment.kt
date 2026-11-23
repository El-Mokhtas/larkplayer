package com.larkplayer.app.presentation.ui.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.larkplayer.app.R
import com.larkplayer.app.databinding.FragmentPlaylistsBinding
import com.larkplayer.app.presentation.adapter.PlaylistAdapter
import com.larkplayer.app.presentation.viewmodel.PlaylistViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaylistsFragment : Fragment(R.layout.fragment_playlists) {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaylistViewModel by viewModels()

    private val adapter = PlaylistAdapter(
        onItemClick = { playlist ->
            val action = PlaylistsFragmentDirections.actionPlaylistsFragmentToPlaylistDetailFragment(playlist.id)
            findNavController().navigate(action)
        },
        onItemLongClick = { playlist ->
            // Show options
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = this@PlaylistsFragment.adapter
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            showCreatePlaylistDialog()
        }
    }

    private fun showCreatePlaylistDialog() {
        val dialog = CreatePlaylistDialogFragment()
        dialog.show(childFragmentManager, "CreatePlaylistDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class CreatePlaylistDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_create_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val etName = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_playlist_name)
        val etDesc = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_playlist_description)
        val btnCreate = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_create)
        val btnCancel = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_cancel)

        btnCreate.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isNotEmpty()) {
                // Create playlist via viewModel
                dismiss()
            }
        }

        btnCancel.setOnClickListener { dismiss() }
    }
}