package com.example.myapplication.ui.image

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ItemImageBinding

class ImageAdapter : ListAdapter<ImageItem, ImageAdapter.ViewHolder>(ImageDiffCallback()) {

    private val selectedImages = mutableSetOf<String>()
    var onSelectionChanged: ((Int) -> Unit)? = null

    // Add this helper function to extract the filename from both types of URIs
    private fun getImageIdentifier(uri: String): String {
        return uri.substringAfterLast("/")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun getSelectedImages(): List<String> {
        // Return the full URIs of selected images
        return currentList.filter { 
            getImageIdentifier(it.uri) in selectedImages 
        }.map { it.uri }
    }

    fun clearSelections() {
        selectedImages.clear()
        notifyDataSetChanged()
        onSelectionChanged?.invoke(0)
    }

    fun preSelectImages(images: List<String>) {
        selectedImages.clear()
        // Store the filenames instead of full paths
        selectedImages.addAll(images.map { getImageIdentifier(it) })
        onSelectionChanged?.invoke(selectedImages.size)
    }

    inner class ViewHolder(private val binding: ItemImageBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    toggleSelection(getItem(position).uri)
                }
            }
        }

        fun bind(item: ImageItem) {
            Glide.with(binding.root)
                .load(item.uri)
                .centerCrop()
                .into(binding.imageView)

            // Compare filenames instead of full paths
            binding.checkView.isVisible = getImageIdentifier(item.uri) in selectedImages
        }

        private fun toggleSelection(uri: String) {
            val identifier = getImageIdentifier(uri)
            if (identifier in selectedImages) {
                selectedImages.remove(identifier)
            } else {
                selectedImages.add(identifier)
            }
            notifyItemChanged(adapterPosition)
            onSelectionChanged?.invoke(selectedImages.size)
        }
    }
}

class ImageDiffCallback : DiffUtil.ItemCallback<ImageItem>() {
    override fun areItemsTheSame(oldItem: ImageItem, newItem: ImageItem): Boolean {
        return oldItem.uri == newItem.uri
    }

    override fun areContentsTheSame(oldItem: ImageItem, newItem: ImageItem): Boolean {
        return oldItem == newItem
    }
}

data class ImageItem(
    val uri: String,
    val name: String
) 