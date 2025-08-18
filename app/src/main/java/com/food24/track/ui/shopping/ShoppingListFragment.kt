package com.food24.track.ui.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.food24.track.data.entity.ShoppingItemEntity
import com.food24.track.databinding.FragmentShoppingListBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ShoppingListFragment : Fragment() {

    private var _binding: FragmentShoppingListBinding? = null
    private val binding get() = _binding!!

    private val vm: ShoppingListViewModel by viewModels()
    private val adapter = ShoppingAdapter(
        onToggle = { vm.toggleItem(it) }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentShoppingListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recycler.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            vm.items.collectLatest { list ->
                binding.emptyView.isVisible = list.isEmpty()
                adapter.submitList(list)
            }
        }
        vm.loadList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class ShoppingAdapter(
        private val onToggle: (ShoppingItemEntity) -> Unit
    ) : ListAdapter<ShoppingItemEntity, ShoppingAdapter.VH>(Diff) {

        object Diff : DiffUtil.ItemCallback<ShoppingItemEntity>() {
            override fun areItemsTheSame(oldItem: ShoppingItemEntity, newItem: ShoppingItemEntity) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: ShoppingItemEntity, newItem: ShoppingItemEntity) = oldItem == newItem
        }

        inner class VH(val item: com.food24.track.databinding.ItemShoppingBinding) :
            RecyclerView.ViewHolder(item.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val inf = LayoutInflater.from(parent.context)
            val binding = com.food24.track.databinding.ItemShoppingBinding.inflate(inf, parent, false)
            return VH(binding)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = getItem(position)
            with(holder.item) {
                textName.text = item.name
                textAmount.text = item.amount
                checkBought.isChecked = item.checked
                root.setOnClickListener { onToggle(item) }
                checkBought.setOnClickListener { onToggle(item) }
            }
        }
    }
}
