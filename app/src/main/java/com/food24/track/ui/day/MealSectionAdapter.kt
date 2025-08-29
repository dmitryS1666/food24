package com.food24.track.ui.day

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.food24.track.R

// ui/day/MealSectionAdapter.kt
class MealSectionAdapter(
    private val onToggle: (Int, Boolean) -> Unit
) : RecyclerView.Adapter<MealSectionAdapter.VH>() {
    private val data = mutableListOf<MealItemUi>()
    fun submit(list: List<MealItemUi>) { data.apply { clear(); addAll(list) }; notifyDataSetChanged() }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH {
        val v = LayoutInflater.from(p.context).inflate(R.layout.item_day_meal, p, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val it = data[pos]
        h.title.text = it.title
        h.kcal.text = "${it.kcal} kcal"
        h.check.setOnCheckedChangeListener(null)
        h.check.isChecked = it.eaten
        h.check.setOnCheckedChangeListener { _, checked -> onToggle(it.id, checked) }
        h.btnView.visibility = View.GONE // при желании оставь
    }

    override fun getItemCount() = data.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.textTitle)
        val kcal: TextView = v.findViewById(R.id.textKcal)
        val check: CheckBox = v.findViewById(R.id.checkEaten)
        val btnView: View = v.findViewById(R.id.btnView)
    }
}
