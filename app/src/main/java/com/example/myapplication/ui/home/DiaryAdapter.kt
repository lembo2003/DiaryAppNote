package com.example.myapplication.ui.home

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import android.widget.LinearLayout
import com.example.myapplication.R
import com.example.myapplication.data.model.DiaryEntry
import com.example.myapplication.databinding.ItemDiaryEntryBinding
import com.example.myapplication.ui.create.Feeling
import java.time.format.DateTimeFormatter

class DiaryAdapter : ListAdapter<DiaryEntry, DiaryAdapter.DiaryViewHolder>(DiaryDiffCallback()) {

    var onEntryClick: ((DiaryEntry) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        return DiaryViewHolder(
            ItemDiaryEntryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DiaryViewHolder(
        private val binding: ItemDiaryEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val imagesAdapter = EntryImagesAdapter()

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEntryClick?.invoke(getItem(position))
                }
            }
            binding.rvImages.apply {
                adapter = imagesAdapter
                addItemDecoration(HorizontalSpaceItemDecoration(dpToPx(8)))
            }
        }

        fun bind(entry: DiaryEntry) {
            binding.apply {
                tvTitle.text = entry.title
                tvContent.text = entry.content
                tvDate.text = entry.date.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy"))
                ivFeeling.setImageResource(Feeling.values().find { 
                    it.name == entry.feeling 
                }?.iconResId ?: R.drawable.ic_feeling_5)

                // Handle images
                val images = entry.images
                if (images.isNotEmpty()) {
                    rvImages.isVisible = true
                    imagesContainer.isVisible = true
                    contentContainer.layoutParams = (contentContainer.layoutParams as LinearLayout.LayoutParams).apply {
                        marginEnd = dpToPx(8)
                    }
                    moreImagesContainer.isVisible = false
                    
                    when {
                        images.size <= 2 -> {
                            rvImages.layoutManager = GridLayoutManager(itemView.context, 1)
                            rvImages.setPadding(0, 0, 0, dpToPx(if (images.size == 2) 4 else 0))
                            imagesAdapter.submitList(images)
                        }
                        images.size == 3 -> {
                            rvImages.layoutManager = GridLayoutManager(itemView.context, 2).apply {
                                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                                    override fun getSpanSize(position: Int) = if (position < 2) 1 else 2
                                }
                            }
                            rvImages.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
                            imagesAdapter.submitList(images)
                        }
                        else -> {
                            rvImages.layoutManager = GridLayoutManager(itemView.context, 2)
                            rvImages.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
                            imagesAdapter.submitList(images.take(4))
                            
                            if (images.size > 4) {
                                moreImagesContainer.isVisible = true
                                moreImagesContainer.bringToFront()
                                tvMoreImages.text = "+${images.size - 4}"
                            }
                        }
                    }
                } else {
                    rvImages.isVisible = false
                    imagesContainer.isVisible = false
                    contentContainer.layoutParams = (contentContainer.layoutParams as LinearLayout.LayoutParams).apply {
                        marginEnd = 0
                    }
                    moreImagesContainer.isVisible = false
                }
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }
}

class DiaryDiffCallback : DiffUtil.ItemCallback<DiaryEntry>() {
    override fun areItemsTheSame(oldItem: DiaryEntry, newItem: DiaryEntry): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DiaryEntry, newItem: DiaryEntry): Boolean {
        return oldItem == newItem
    }
} 