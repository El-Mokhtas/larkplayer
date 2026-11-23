package com.larkplayer.app.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.larkplayer.app.R
import com.larkplayer.app.data.model.Playlist
import com.larkplayer.app.databinding.ItemPlaylistBinding

class PlaylistAdapter(
    private val onItemClick: (Playlist) -> Unit,
    private val onItemLongClick: ((Playlist) -> Unit)? = null
) : ListAdapter<Playlist, PlaylistAdapter.PlaylistViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class PlaylistViewHolder(private val binding: ItemPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener { onItemClick(getItem(adapterPosition)) }
            binding.root.setOnLongClickListener {
                onItemLongClick?.invoke(getItem(adapterPosition))
                true
            }
        }

        fun bind(item: Playlist) {
            binding.tvName.text = item.name
            binding.tvCount.text = "${getItemCount(item)} tracks"

            if (item.isSystemPlaylist) {
                binding.ivSystemIndicator.visibility = View.VISIBLE
                when (item.name) {
                    "Liked Songs" -> binding.ivIcon.setImageResource(R.drawable.ic_favorite_filled)
                    "Recently Played" -> binding.ivIcon.setImageResource(R.drawable.ic_history)
                    "Recently Added" -> binding.ivIcon.setImageResource(R.drawable.ic_new_releases)
                }
            } else {
                binding.ivSystemIndicator.visibility = View.GONE
                item.artworkUrl?.let { uri ->
                    Glide.with(binding.root)
                        .load(uri)
                        .placeholder(R.drawable.ic_music_note)
                        .error(R.drawable.ic_music_note)
                        .into(binding.ivIcon)
                } ?: run {
                    binding.ivIcon.setImageResource(R.drawable.ic_playlist)
                }
            }
        }

        private fun getItemCount(playlist: Playlist): Int {
            // Would need to query actual count from database
            return 0
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem == newItem
        }
    }
}