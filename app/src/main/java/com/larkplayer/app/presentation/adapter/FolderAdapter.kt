package com.larkplayer.app.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.larkplayer.app.R
import com.larkplayer.app.data.model.Folder
import com.larkplayer.app.databinding.ItemFolderBinding

class FolderAdapter(
    private val onItemClick: (Folder) -> Unit,
    private val onItemLongClick: ((Folder) -> Unit)? = null
) : ListAdapter<Folder, FolderAdapter.FolderViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val binding = ItemFolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FolderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class FolderViewHolder(private val binding: ItemFolderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener { onItemClick(getItem(adapterPosition)) }
            binding.root.setOnLongClickListener {
                onItemLongClick?.invoke(getItem(adapterPosition))
                true
            }
        }

        fun bind(item: Folder) {
            binding.tvName.text = item.displayName
            binding.tvCount.text = "${item.mediaCount} files"

            // Try to load folder artwork from first media file
            binding.ivIcon.setImageResource(R.drawable.ic_folder)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Folder>() {
        override fun areItemsTheSame(oldItem: Folder, newItem: Folder): Boolean {
            return oldItem.path == newItem.path
        }

        override fun areContentsTheSame(oldItem: Folder, newItem: Folder): Boolean {
            return oldItem == newItem
        }
    }
}