package com.food24.track.ui.day

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.food24.track.R
import com.food24.track.ui.day.DaySectionUi

class DaySectionsAdapter(
    private val onOpenSection: (String) -> Unit   // MealTypes.*
) : RecyclerView.Adapter<DaySectionsAdapter.VH>() {

    private val items = mutableListOf<DaySectionUi>()

    fun submit(newList: List<DaySectionUi>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH {
        val v = LayoutInflater.from(p.context).inflate(R.layout.item_day_section, p, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = items[pos]
        h.img.setImageResource(item.thumbRes)
        h.title.text = item.title
        h.meta.text = buildString {
            append(item.totalKcal).append(" kcal • ").append(item.itemsCount).append(" items")
            if (item.eatenCount > 0) append(" (").append(item.eatenCount).append(" eaten)")
        }

        h.itemView.setOnClickListener { onOpenSection(item.type) }
        h.btnView.setOnClickListener { onOpenSection(item.type) }
    }

    override fun getItemCount() = items.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.img)
        val title: TextView = v.findViewById(R.id.textTitle)
        val meta: TextView = v.findViewById(R.id.textMeta)
        val btnView: View = v.findViewById(R.id.btnView)
    }
}
