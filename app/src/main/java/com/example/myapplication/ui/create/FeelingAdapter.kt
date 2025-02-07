package com.example.myapplication.ui.create

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemFeelingBinding

class FeelingAdapter : RecyclerView.Adapter<FeelingAdapter.FeelingViewHolder>() {

    private var selectedPosition = -1
    var isEnabled = true
    var onFeelingSelected: ((Feeling) -> Unit)? = null
    private val feelings = Feeling.values()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeelingViewHolder {
        return FeelingViewHolder(
            ItemFeelingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FeelingViewHolder, position: Int) {
        holder.bind(feelings[position], position == selectedPosition)
    }

    override fun getItemCount() = feelings.size

    fun getSelectedFeeling(): Feeling? {
        return if (selectedPosition != -1) feelings[selectedPosition] else null
    }

    fun setSelectedFeeling(feeling: Feeling?) {
        selectedPosition = feeling?.let { Feeling.values().indexOf(it) } ?: -1
        notifyDataSetChanged()
    }

    inner class FeelingViewHolder(
        private val binding: ItemFeelingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (isEnabled) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val oldPosition = selectedPosition
                        selectedPosition = if (selectedPosition == position) -1 else position
                        notifyItemChanged(oldPosition)
                        notifyItemChanged(selectedPosition)
                        onFeelingSelected?.invoke(feelings[position])
                    }
                }
            }
        }

        fun bind(feeling: Feeling, isSelected: Boolean) {
            binding.apply {
                ivFeeling.setImageResource(feeling.iconResId)
                root.isSelected = isSelected
            }
        }
    }
} 