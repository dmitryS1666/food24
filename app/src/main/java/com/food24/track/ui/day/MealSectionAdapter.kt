package com.food24.track.ui.day

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.food24.track.R

class MealSectionAdapter(
    private val onToggle: (mealId: Int, eaten: Boolean) -> Unit
) : RecyclerView.Adapter<MealSectionAdapter.VH>() {

    private val items = mutableListOf<MealRowUi>()
    fun submit(newItems: List<MealRowUi>) { items.apply { clear(); addAll(newItems) }; notifyDataSetChanged() }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH {
        val v = LayoutInflater.from(p.context).inflate(R.layout.item_meal_section, p, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val it = items[pos]
        h.title.text = it.title
        h.kcal.text = "${it.calories} kcal"
        h.check.setOnCheckedChangeListener(null)
        h.check.isChecked = it.eaten
        h.check.setOnCheckedChangeListener { _, checked -> onToggle(it.mealId, checked) }
    }

    override fun getItemCount() = items.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.textTitle)
        val kcal: TextView = v.findViewById(R.id.textKcal)
        val check: CheckBox = v.findViewById(R.id.checkEaten)
    }
}

