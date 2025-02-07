package com.example.myapplication.ui.language

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemLanguageBinding

class LanguageAdapter : ListAdapter<Language, LanguageAdapter.LanguageViewHolder>(LanguageDiffCallback()) {

    private var selectedPosition = 0
    var onLanguageSelected: ((Language) -> Unit)? = null

    fun setSelectedLanguage(countryCode: String) {
        val newPosition = currentList.indexOfFirst { it.countryCode == countryCode }
        if (newPosition != -1) {
            val oldPosition = selectedPosition
            selectedPosition = newPosition
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        return LanguageViewHolder(
            ItemLanguageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LanguageViewHolder(
        private val binding: ItemLanguageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            // Click listener for the whole item
            binding.root.setOnClickListener {
                handleSelection()
            }

            // Click listener for the radio button
            binding.rbSelect.setOnClickListener {
                handleSelection()
            }
        }

        private fun handleSelection() {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val oldPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)
                onLanguageSelected?.invoke(getItem(position))
            }
        }

        fun bind(language: Language) {
            binding.apply {
                tvLanguage.text = language.name
                rbSelect.isChecked = adapterPosition == selectedPosition
                
                // Load flag image
                val flagResId = root.context.resources.getIdentifier(
                    "flag_${language.countryCode}",
                    "drawable",
                    root.context.packageName
                )
                if (flagResId != 0) {
                    ivFlag.setImageResource(flagResId)
                }

                // Add selection animation
                root.animate()
                    .scaleX(if (rbSelect.isChecked) 1.02f else 1f)
                    .scaleY(if (rbSelect.isChecked) 1.02f else 1f)
                    .setDuration(200)
                    .start()

                // Make radio button clickable and add ripple
                rbSelect.isClickable = true
                rbSelect.isFocusable = true
            }
        }
    }
}

class LanguageDiffCallback : DiffUtil.ItemCallback<Language>() {
    override fun areItemsTheSame(oldItem: Language, newItem: Language): Boolean {
        return oldItem.countryCode == newItem.countryCode
    }

    override fun areContentsTheSame(oldItem: Language, newItem: Language): Boolean {
        return oldItem == newItem
    }
}

data class Language(
    val name: String,
    val countryCode: String,
    val isSelected: Boolean
) 