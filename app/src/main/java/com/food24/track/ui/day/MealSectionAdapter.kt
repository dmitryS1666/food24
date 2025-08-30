package com.food24.track.ui.day

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class MealSectionAdapter(
    private val onToggle: (mealId: Int, eaten: Boolean) -> Unit
) : RecyclerView.Adapter<MealSectionAdapter.VH>() {

    private val items = mutableListOf<MealItemUi>()

    fun submit(newList: List<MealItemUi>) {
        items.clear(); items.addAll(newList); notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = com.food24.track.databinding.ItemMealSectionBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val it = items[position]
        with(h.b) {
            textTitle.text = it.title
            textKcal.text = "${it.calories} kcal"
            // чтобы не ловить ложные вызовы listener при реюзе:
            checkEaten.setOnCheckedChangeListener(null)
            checkEaten.isChecked = it.eaten
            checkEaten.setOnCheckedChangeListener { _, checked ->
                onToggle(it.mealId, checked)
            }
        }
    }

    override fun getItemCount() = items.size

    class VH(val b: com.food24.track.databinding.ItemMealSectionBinding)
        : RecyclerView.ViewHolder(b.root)
}
