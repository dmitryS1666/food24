package com.food24.track.ui.shopping

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.food24.track.databinding.ItemCategoryHeaderBinding
import com.food24.track.databinding.ItemShoppingBinding

sealed class Row {
    data class Category(val title: String, val iconRes: Int, var expanded: Boolean) : Row()
    data class Item(
        val id: Int,
        val name: String,
        val amount: String,
        val category: String,
        val checked: Boolean
    ) : Row()
}

class ShoppingSectionAdapter(
    private val onToggle: (id: Int, checked: Boolean) -> Unit,
    private val onHeaderClick: (position: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val rows = mutableListOf<Row>()

    fun submit(newRows: List<Row>) {
        rows.clear()
        rows.addAll(newRows)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) =
        when (rows[position]) {
            is Row.Category -> 0
            is Row.Item -> 1
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == 0) {
            val b = ItemCategoryHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            HeaderVH(b)
        } else {
            val b = ItemShoppingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ItemVH(b)
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is Row.Category -> (holder as HeaderVH).bind(row, position, onHeaderClick)
            is Row.Item -> (holder as ItemVH).bind(row, onToggle)
        }
    }

    override fun getItemCount() = rows.size

    class HeaderVH(private val b: ItemCategoryHeaderBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(row: Row.Category, pos: Int, onClick: (Int) -> Unit) {
            b.title.text = row.title
            b.icon.setImageResource(row.iconRes)
            b.chevron.rotation = if (row.expanded) 180f else 0f
            b.root.setOnClickListener { onClick(pos) }
        }
    }

    class ItemVH(private val b: ItemShoppingBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(row: Row.Item, onToggle: (Int, Boolean) -> Unit) {
            val cb: CheckBox = b.checkBox
            cb.text = buildString {
                append(row.name)
                if (row.amount.isNotBlank()) append(" — ${row.amount}")
            }
            cb.setOnCheckedChangeListener(null)
            cb.isChecked = row.checked
            cb.setOnCheckedChangeListener { _, checked -> onToggle(row.id, checked) }
        }
    }
}
