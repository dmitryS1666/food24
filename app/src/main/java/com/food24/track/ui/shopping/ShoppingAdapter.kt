package com.food24.track.ui.shopping

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.food24.track.data.entity.ShoppingItemEntity
import com.food24.track.databinding.ItemShoppingBinding

class ShoppingAdapter(
    private val onToggle: (Int, Boolean) -> Unit
) : ListAdapter<ShoppingItemEntity, ShoppingAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<ShoppingItemEntity>() {
        override fun areItemsTheSame(o: ShoppingItemEntity, n: ShoppingItemEntity) = o.id == n.id
        override fun areContentsTheSame(o: ShoppingItemEntity, n: ShoppingItemEntity) = o == n
    }

    inner class VH(private val b: ItemShoppingBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(row: ShoppingItemEntity) {
            b.tvNameAmount.text = buildString {
                append(row.name)
                if (row.amount.isNotBlank()) append(": ").append(row.amount)
            }
            b.checkBox.setOnCheckedChangeListener(null)
            b.checkBox.isChecked = row.checked
            b.checkBox.setOnCheckedChangeListener { _, checked ->
                onToggle(row.id, checked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemShoppingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}
