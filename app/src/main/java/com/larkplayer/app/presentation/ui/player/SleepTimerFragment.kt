package com.larkplayer.app.presentation.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.larkplayer.app.R
import com.larkplayer.app.databinding.FragmentSleepTimerBinding
import com.larkplayer.app.presentation.viewmodel.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SleepTimerFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSleepTimerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayerViewModel by viewModels()

    private val timerOptions = listOf(
        TimerOption("Off", 0),
        TimerOption("10 min", 10),
        TimerOption("20 min", 20),
        TimerOption("30 min", 30),
        TimerOption("45 min", 45),
        TimerOption("60 min", 60),
        TimerOption("90 min", 90),
        TimerOption("120 min", 120),
        TimerOption("End of Track", -1)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSleepTimerBinding.inflate(inflater, container, false)
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
            adapter = TimerAdapter(timerOptions) { minutes ->
                onTimerSelected(minutes)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.sleepTimerRemaining.collect { remaining ->
            updateSelection(remaining)
        }
    }

    private fun updateSelection(remaining: Long?) {
        val adapter = binding.recyclerView.adapter as? TimerAdapter
        adapter?.notifyDataSetChanged()
    }

    private fun onTimerSelected(minutes: Long) {
        if (minutes == -1) {
            // End of track - would need special handling
            viewModel.setSleepTimer(0)
        } else {
            viewModel.setSleepTimer(minutes)
        }
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class TimerAdapter(
        private val options: List<TimerOption>,
        private val onClick: (Long) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<TimerAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            val tvLabel: android.widget.TextView = view.findViewById(R.id.tv_label)
            val ivCheck: android.widget.ImageView = view.findViewById(R.id.iv_check)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_timer_option, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val option = options[position]
            holder.tvLabel.text = option.label
            // Check if this is the selected option (simplified)
        }

        override fun getItemCount(): Int = options.size
    }

    private data class TimerOption(val label: String, val minutes: Long)
}