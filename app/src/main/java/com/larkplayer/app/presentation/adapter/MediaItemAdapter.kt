package com.larkplayer.app.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.larkplayer.app.R
import com.larkplayer.app.data.model.MediaItem
import com.larkplayer.app.databinding.ItemMediaBinding

class MediaItemAdapter(
    private val onItemClick: (MediaItem) -> Unit,
    private val onItemLongClick: ((MediaItem) -> Unit)? = null,
    private val onMoreClick: ((MediaItem) -> Unit)? = null
) : ListAdapter<MediaItem, MediaItemAdapter.MediaViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class MediaViewHolder(private val binding: ItemMediaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener { onItemClick(getItem(adapterPosition)) }
            binding.root.setOnLongClickListener {
                onItemLongClick?.invoke(getItem(adapterPosition))
                true
            }
            binding.btnMore.setOnClickListener { onMoreClick?.invoke(getItem(adapterPosition)) }
        }

        fun bind(item: MediaItem) {
            binding.tvTitle.text = item.title
            binding.tvArtist.text = item.artist ?: "Unknown Artist"
            binding.tvDuration.text = item.formattedDuration

            // Load artwork
            item.artworkUrl?.let { uri ->
                Glide.with(binding.root)
                    .load(uri)
                    .placeholder(R.drawable.ic_music_note)
                    .error(R.drawable.ic_music_note)
                    .into(binding.ivArtwork)
            } ?: run {
                binding.ivArtwork.setImageResource(R.drawable.ic_music_note)
            }

            // Favorite indicator
            binding.ivFavorite.visibility = if (item.isFavorite) View.VISIBLE else View.GONE
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MediaItem>() {
        override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
            return oldItem == newItem
        }
    }
}