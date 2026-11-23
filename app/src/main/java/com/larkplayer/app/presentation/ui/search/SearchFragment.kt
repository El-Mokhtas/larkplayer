package com.larkplayer.app.presentation.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.larkplayer.app.R
import com.larkplayer.app.databinding.FragmentSearchBinding
import com.larkplayer.app.presentation.adapter.MediaItemAdapter
import com.larkplayer.app.presentation.viewmodel.LibraryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LibraryViewModel by viewModels()

    private val audioAdapter = MediaItemAdapter(
        onItemClick = { media ->
            val action = SearchFragmentDirections.actionSearchFragmentToPlayerFragment(media.id)
            findNavController().navigate(action)
        }
    )

    private val videoAdapter = MediaItemAdapter(
        onItemClick = { media ->
            val action = SearchFragmentDirections.actionSearchFragmentToPlayerFragment(media.id)
            findNavController().navigate(action)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeSearch()
    }

    private fun setupViews() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                viewModel.onSearchQueryChanged(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                viewModel.onSearchQueryChanged(newText)
                return true
            }
        })

        binding.searchView.setOnCloseListener {
            viewModel.clearSearch()
            false
        }

        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                updateTabContent(tab.position)
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
        })

        binding.rvAudio.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = audioAdapter
        }

        binding.rvVideo.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = videoAdapter
        }
    }

    private fun updateTabContent(tabPosition: Int) {
        binding.rvAudio.visibility = if (tabPosition == 0) View.VISIBLE else View.GONE
        binding.rvVideo.visibility = if (tabPosition == 1) View.VISIBLE else View.GONE
        binding.emptyView.visibility = View.GONE
    }

    private fun observeSearch() {
        viewModel.searchResultsAudio.collect { items ->
            audioAdapter.submitData(lifecycleScope.coroutineContext, items)
        }

        viewModel.searchResultsVideo.collect { items ->
            videoAdapter.submitData(lifecycleScope.coroutineContext, items)
        }

        viewModel.searchQuery.collect { query ->
            val hasResults = (audioAdapter.currentList.isNotEmpty() || videoAdapter.currentList.isNotEmpty())
            binding.emptyView.visibility = if (query.isNotEmpty() && !hasResults) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}