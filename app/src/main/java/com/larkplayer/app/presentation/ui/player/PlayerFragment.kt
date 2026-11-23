package com.larkplayer.app.presentation.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.larkplayer.app.R
import com.larkplayer.app.databinding.FragmentPlayerBinding
import com.larkplayer.app.presentation.viewmodel.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlayerFragment : Fragment(R.layout.fragment_player) {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayerViewModel by viewModels()
    private val args: PlayerArgs by lazy {
        PlayerArgs(arguments?.getLong("mediaId", 0L) ?: 0L)
    }

    private data class PlayerArgs(val mediaId: Long)

    private var isFullscreen = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
        loadMedia(args.mediaId)
    }

    private fun setupViews() {
        // Play/Pause
        binding.btnPlayPause.setOnClickListener {
            // Toggle play/pause via service
        }

        // Next/Previous
        binding.btnNext.setOnClickListener { /* next track */ }
        binding.btnPrevious.setOnClickListener { /* previous track */ }

        // Shuffle/Repeat
        binding.btnShuffle.setOnClickListener { /* toggle shuffle */ }
        binding.btnRepeat.setOnClickListener { /* toggle repeat */ }

        // Seekbar
        binding.seekbar.setOnSeekBarChangeListener(object : androidx.appcompat.widget.AppCompatSeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: androidx.appcompat.widget.AppCompatSeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // Update position preview
                }
            }

            override fun onStartTrackingTouch(seekBar: androidx.appcompat.widget.AppCompatSeekBar) {}

            override fun onStopTrackingTouch(seekBar: androidx.appcompat.widget.AppCompatSeekBar) {
                // Seek to position
            }
        })

        // More options
        binding.btnMore.setOnClickListener { /* show bottom sheet */ }
        binding.btnEqualizer.setOnClickListener { openEqualizer() }
        binding.btnLyrics.setOnClickListener { openLyrics() }
        binding.btnQueue.setOnClickListener { openQueue() }
        binding.btnSleepTimer.setOnClickListener { openSleepTimer() }
        binding.btnSpeed.setOnClickListener { openSpeed() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.currentMedia.collect { media ->
                media?.let { updateUI(it) }
            }
        }

        lifecycleScope.launch {
            viewModel.isPlaying.collect { isPlaying ->
                updatePlayPauseButton(isPlaying)
                animateArtwork(isPlaying)
            }
        }

        lifecycleScope.launch {
            viewModel.currentPosition.combine(viewModel.duration) { pos, dur ->
                updateProgress(pos, dur)
            }.collect { }
        }

        lifecycleScope.launch {
            viewModel.playbackSpeed.collect { speed ->
                binding.tvSpeed.text = String.format("%.2fx", speed)
            }
        }

        lifecycleScope.launch {
            viewModel.currentLyricIndex.collect { index ->
                updateLyricsDisplay(index)
            }
        }
    }

    private fun loadMedia(mediaId: Long) {
        viewModel.loadMedia(mediaId)
    }

    private fun updateUI(media: com.larkplayer.app.data.model.MediaItem) {
        binding.tvTitle.text = media.title
        binding.tvArtist.text = media.artist ?: "Unknown Artist"

        media.artworkUrl?.let { uri ->
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_music_note)
                .error(R.drawable.ic_music_note)
                .into(binding.ivArtwork)
            Glide.with(this)
                .load(uri)
                .placeholder(R.color.surface_variant)
                .into(binding.ivBackground)
        }
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        val iconRes = if (isPlaying) R.drawable.ic_pause_large else R.drawable.ic_play_large
        binding.btnPlayPause.setImageResource(iconRes)
    }

    private fun animateArtwork(isPlaying: Boolean) {
        if (isPlaying) {
            binding.ivArtwork.animate().rotationBy(360f).setDuration(20000).setInterpolator(android.view.animation.LinearInterpolator()).start()
        } else {
            binding.ivArtwork.animate().cancel()
        }
    }

    private fun updateProgress(position: Long, duration: Long) {
        if (duration > 0) {
            val progress = (position * 100 / duration).toInt()
            binding.seekbar.progress = progress
            binding.tvCurrentTime.text = formatTime(position)
            binding.tvTotalTime.text = formatTime(duration)
        }
    }

    private fun updateLyricsDisplay(index: Int) {
        // Update lyrics view if visible
    }

    private fun openEqualizer() {
        findNavController().navigate(R.id.action_playerFragment_to_equalizerFragment)
    }

    private fun openLyrics() {
        findNavController().navigate(R.id.action_playerFragment_to_lyricsFragment)
    }

    private fun openQueue() {
        findNavController().navigate(R.id.action_playerFragment_to_queueFragment)
    }

    private fun openSleepTimer() {
        findNavController().navigate(R.id.action_playerFragment_to_sleepTimerFragment)
    }

    private fun openSpeed() {
        findNavController().navigate(R.id.action_playerFragment_to_speedFragment)
    }

    private fun formatTime(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}