package com.larkplayer.app.presentation.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.larkplayer.app.R
import com.larkplayer.app.databinding.FragmentSpeedBinding
import com.larkplayer.app.presentation.viewmodel.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SpeedFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSpeedBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayerViewModel by viewModels()

    private val speeds = listOf(
        SpeedOption("0.5x", 0.5f),
        SpeedOption("0.75x", 0.75f),
        SpeedOption("1.0x", 1.0f),
        SpeedOption("1.25x", 1.25f),
        SpeedOption("1.5x", 1.5f),
        SpeedOption("2.0x", 2.0f)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = SpeedAdapter(speeds) { speed ->
                viewModel.setPlaybackSpeed(speed)
                dismiss()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.playbackSpeed.collect { currentSpeed ->
            val adapter = binding.recyclerView.adapter as? SpeedAdapter
            adapter?.setSelectedSpeed(currentSpeed)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class SpeedAdapter(
        private val options: List<SpeedOption>,
        private val onClick: (Float) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<SpeedAdapter.ViewHolder>() {

        private var selectedSpeed = 1.0f

        inner class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            val tvLabel: android.widget.TextView = view.findViewById(R.id.tv_label)
            val ivCheck: android.widget.ImageView = view.findViewById(R.id.iv_check)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_speed_option, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val option = options[position]
            holder.tvLabel.text = option.label
            val isSelected = option.speed == selectedSpeed
            holder.ivCheck.visibility = if (isSelected) View.VISIBLE else View.GONE
            holder.itemView.setOnClickListener { onClick(option.speed) }
        }

        override fun getItemCount(): Int = options.size

        fun setSelectedSpeed(speed: Float) {
            selectedSpeed = speed
            notifyDataSetChanged()
        }
    }

    private data class SpeedOption(val label: String, val speed: Float)
}