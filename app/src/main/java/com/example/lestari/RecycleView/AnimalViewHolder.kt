package com.example.lestari.RecycleView

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lestari.DetailDataActivity
import com.example.lestari.databinding.ListHistoryBinding

class AnimalViewHolder(private val binding: ListHistoryBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(animalData: animal, clickListener: (animal: animal) -> Unit) {
        // Bind data to views
        Glide.with(itemView.context)
            .load(animalData.animal_picture)
            .into(binding.ivListHistory)

        binding.tvLocalHistory.text = animalData.local_name
        binding.tvLatinHistory.text = animalData.latin_name
        binding.tvLocHistory.text = animalData.city
        binding.tvLongLangHistory.text = "${animalData.longitude}, ${animalData.latitude}"
        itemView.setOnClickListener {
            // Handle item click, e.g., start DetailActivity with item data
            val intent = Intent(itemView.context, DetailDataActivity::class.java)
            intent.putExtra("animalData", animalData)
            itemView.context.startActivity(intent)
        }
    }

    companion object {
        fun create(parent: ViewGroup): AnimalViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ListHistoryBinding.inflate(inflater, parent, false)
            return AnimalViewHolder(binding)
        }
    }
}