package com.example.lestari.RecycleView

import android.content.Intent
import android.view.ViewGroup
import android.widget.Filter
import androidx.recyclerview.widget.ListAdapter
import com.example.lestari.EditDataActivity

class AnimalAdapter : ListAdapter<animal, AnimalViewHolder>(AnimalDiffCallback()) {

    private val originalList: MutableList<animal> = mutableListOf()

    init {
        // Copy the original list for filtering
        originalList.addAll(currentList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalViewHolder {
        return AnimalViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: AnimalViewHolder, position: Int) {
        val animal = getItem(position)
        holder.bind(animal) { clickedAnimal ->
            // Handle item click, start new activity with clickedAnimal data
            val intent = Intent(holder.itemView.context, EditDataActivity::class.java)
            intent.putExtra("animalData", clickedAnimal)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun submitList(list: List<animal>?) {
        super.submitList(list)
        originalList.clear()
        originalList.addAll(list ?: emptyList())
    }

    val filter: Filter
        get() = object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = mutableListOf<animal>()

                if (constraint.isNullOrBlank()) {
                    filteredList.addAll(originalList)
                } else {
                    val filterPattern = constraint.toString().toLowerCase().trim()

                    for (item in originalList) {
                        if (item.latin_name!!.toLowerCase().contains(filterPattern)) {
                            filteredList.add(item)
                        }
                    }
                }

                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                submitList(results?.values as? List<animal>)
            }
        }

    // Helper function to reset the adapter to the original list
    fun resetAdapter() {
        submitList(originalList)
    }
}
