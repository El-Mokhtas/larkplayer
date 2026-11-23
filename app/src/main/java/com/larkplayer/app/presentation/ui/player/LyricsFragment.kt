package com.larkplayer.app.presentation.ui.player

import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.larkplayer.app.R
import com.larkplayer.app.databinding.FragmentLyricsBinding
import com.larkplayer.app.presentation.viewmodel.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LyricsFragment : Fragment(R.layout.fragment_lyrics) {

    private var _binding: FragmentLyricsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLyricsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeLyrics()
    }

    private fun observeLyrics() {
        viewModel.lyrics.combine(viewModel.currentLyricIndex) { lyrics, currentIndex ->
            updateLyricsDisplay(lyrics, currentIndex)
        }.collect { }
    }

    private fun updateLyricsDisplay(lyrics: List<com.larkplayer.app.util.LyricLine>, currentIndex: Int) {
        if (lyrics.isEmpty()) {
            binding.tvEmptyLyrics.visibility = View.VISIBLE
            binding.tvLyrics.text = ""
            return
        }

        binding.tvEmptyLyrics.visibility = View.GONE

        val builder = StringBuilder()
        for (i in lyrics.indices) {
            val line = lyrics[i]
            builder.append(line.text)
            if (i < lyrics.lastIndex) builder.append("\n\n")
        }

        val spannable = SpannableString(builder.toString())

        // Highlight current line
        if (currentIndex >= 0 && currentIndex < lyrics.size) {
            var start = 0
            for (i in 0 until currentIndex) {
                start += lyrics[i].text.length + 2 // +2 for \n\n
            }
            val end = start + lyrics[currentIndex].text.length
            if (end <= spannable.length) {
                spannable.setSpan(
                    ForegroundColorSpan(requireContext().getColor(R.color.primary)),
                    start,
                    end,
                    0
                )
            }
        }

        binding.tvLyrics.text = spannable
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}