package com.example.myapplication.ui.diary

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ItemSelectedImageBinding
import java.io.File

class SelectedImagesAdapter : ListAdapter<String, SelectedImagesAdapter.ViewHolder>(DiffCallback()) {

    var isEditMode: Boolean = false
    var onImageClick: ((String) -> Unit)? = null
    var onDeleteClick: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSelectedImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemSelectedImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onImageClick?.invoke(getItem(position))
                }
            }

            binding.btnRemoveImage.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick?.invoke(getItem(position))
                }
            }
        }

        fun bind(uri: String) {
            binding.apply {
                // Load image
                Glide.with(imageView)
                    .load(uri)
                    .centerCrop()
                    .into(imageView)

                // Show delete button only in edit mode
                btnRemoveImage.isVisible = isEditMode
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
} 