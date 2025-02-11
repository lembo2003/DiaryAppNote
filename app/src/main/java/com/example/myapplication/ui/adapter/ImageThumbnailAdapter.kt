package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ItemImageThumbnailBinding

class ImageThumbnailAdapter : ListAdapter<String, ImageThumbnailAdapter.ViewHolder>(DiffCallback()) {

    var onThumbnailClick: ((String) -> Unit)? = null
    var selectedPosition = 0
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemImageThumbnailBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position == selectedPosition)
    }

    fun setSelectedImage(uri: String) {
        val newPosition = currentList.indexOf(uri)
        if (newPosition != -1) {
            val oldPosition = selectedPosition
            selectedPosition = newPosition
            notifyItemChanged(oldPosition)
            notifyItemChanged(newPosition)
        }
    }

    inner class ViewHolder(
        private val binding: ItemImageThumbnailBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onThumbnailClick?.invoke(getItem(position))
                }
            }
        }

        fun bind(uri: String, isSelected: Boolean) {
            binding.root.alpha = if (isSelected) 1f else 0.6f
            Glide.with(binding.root)
                .load(uri)
                .centerCrop()
                .into(binding.ivThumbnail)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    }
} 