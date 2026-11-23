package com.larkplayer.app.presentation.ui.player

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.larkplayer.app.R
import com.larkplayer.app.databinding.MiniPlayerViewBinding
import com.larkplayer.app.data.model.MediaItem

class MiniPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: MiniPlayerViewBinding = MiniPlayerViewBinding.inflate(LayoutInflater.from(context), this, true)

    var onPlayPauseClick: (() -> Unit)? = null
    var onNextClick: (() -> Unit)? = null
    var onExpandClick: (() -> Unit)? = null

    init {
        setupViews()
    }

    private fun setupViews() {
        binding.root.setOnClickListener { onExpandClick?.invoke() }
        binding.btnPlayPause.setOnClickListener { onPlayPauseClick?.invoke() }
        binding.btnNext.setOnClickListener { onNextClick?.invoke() }
        binding.btnClose.setOnClickListener { visibility = View.GONE }
    }

    fun bind(media: MediaItem, isPlaying: Boolean) {
        binding.tvTitle.text = media.title
        binding.tvArtist.text = media.artist ?: "Unknown Artist"

        media.artworkUrl?.let { uri ->
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_music_note)
                .error(R.drawable.ic_music_note)
                .into(binding.ivArtwork)
        } ?: run {
            binding.ivArtwork.setImageResource(R.drawable.ic_music_note)
        }

        updatePlayPauseButton(isPlaying)
        animateArtwork(isPlaying)
    }

    fun updatePlayPauseButton(isPlaying: Boolean) {
        val iconRes = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        binding.btnPlayPause.setImageResource(iconRes)
    }

    private fun animateArtwork(isPlaying: Boolean) {
        if (isPlaying) {
            binding.ivArtwork.animate().rotationBy(360f).setDuration(20000).setInterpolator(android.view.animation.LinearInterpolator()).start()
        } else {
            binding.ivArtwork.animate().cancel()
        }
    }

    fun show() {
        visibility = View.VISIBLE
        alpha = 0f
        animate().alpha(1f).setDuration(300).start()
    }

    fun hide() {
        animate().alpha(0f).setDuration(300).withEndAction { visibility = View.GONE }.start()
    }
}