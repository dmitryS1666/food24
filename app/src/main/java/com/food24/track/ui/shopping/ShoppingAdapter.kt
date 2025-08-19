package com.food24.track.ui.shopping

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.food24.track.databinding.ItemShoppingBinding

class ShoppingAdapter(
    private val onChecked: (id: Int, checked: Boolean) -> Unit
) : RecyclerView.Adapter<ShoppingAdapter.VH>() {

    private val items = mutableListOf<UiShoppingItem>()

    fun submit(newItems: List<UiShoppingItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemShoppingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val it = items[position]
        with(holder.b.checkBox) {
            text = buildString {
                append(it.name)
                if (it.amount.isNotBlank()) append(" — ${it.amount}")
                if (it.category.isNotBlank()) append("  (${it.category})")
            }
            setOnCheckedChangeListener(null)
            isChecked = it.checked
            setOnCheckedChangeListener { _, checked -> onChecked(it.id, checked) }
        }
    }

    override fun getItemCount() = items.size

    class VH(val b: ItemShoppingBinding) : RecyclerView.ViewHolder(b.root)
}
