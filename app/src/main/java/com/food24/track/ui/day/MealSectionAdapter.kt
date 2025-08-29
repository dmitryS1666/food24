package com.food24.track.ui.day

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.food24.track.R

class MealSectionAdapter(
    private val onToggle: (mealId: Int, newValue: Boolean, revert: () -> Unit) -> Unit
) : RecyclerView.Adapter<MealSectionAdapter.VH>() {

    private val items = mutableListOf<MealRowUi>()
    fun submit(newItems: List<MealRowUi>) { items.apply { clear(); addAll(newItems) }; notifyDataSetChanged() }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH {
        val v = LayoutInflater.from(p.context).inflate(R.layout.item_meal_section, p, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val it = items[pos]
        h.textTitle.text = it.title
        h.textKcal.text = "${it.calories} kcal"
        h.checkEaten.setOnCheckedChangeListener(null)
        h.checkEaten.isChecked = it.eaten
        h.checkEaten.setOnCheckedChangeListener { _, checked ->
            // передаем наружу + даем колбэк на откат UI
            onToggle(it.mealId, checked) {
                // вернуть чекбокс назад
                h.checkEaten.setOnCheckedChangeListener(null)
                h.checkEaten.isChecked = !checked
                h.checkEaten.setOnCheckedChangeListener { _, c -> onToggle(it.mealId, c) { /* no-op */ } }
            }
        }
    }

    override fun getItemCount() = items.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val textTitle: TextView = v.findViewById(R.id.textTitle)
        val textKcal: TextView = v.findViewById(R.id.textKcal)
        val checkEaten: CheckBox = v.findViewById(R.id.checkEaten)
    }
}

