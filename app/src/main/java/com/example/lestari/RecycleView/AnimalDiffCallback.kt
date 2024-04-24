package com.example.lestari.RecycleView

import androidx.recyclerview.widget.DiffUtil

class AnimalDiffCallback : DiffUtil.ItemCallback<animal>() {

    override fun areItemsTheSame(oldItem: animal, newItem: animal): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: animal, newItem: animal): Boolean {
        return oldItem == newItem
    }
}
