package com.food24.track.ui.progress

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.food24.track.data.entity.ProgressEntryEntity
import com.food24.track.databinding.FragmentProgressBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    private val vm: ProgressViewModel by viewModels()
    private val adapter = ProgressAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recycler.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireContext())   // <- layoutManager
        binding.recycler.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            vm.progress.collectLatest { list ->
                binding.emptyView.isVisible = list.isEmpty()
                adapter.submitList(list)
            }
        }
        vm.loadProgress()

        binding.btnAdd.setOnClickListener {
            val weight = binding.editWeight.text.toString().toFloatOrNull() ?: return@setOnClickListener
            val date = binding.editDate.text.toString().ifBlank { java.time.LocalDate.now().toString() }
            vm.addEntry(ProgressEntryEntity(date = date, weight = weight, caloriesConsumed = 0))
            binding.editWeight.setText("")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class ProgressAdapter : ListAdapter<ProgressEntryEntity, ProgressAdapter.VH>(Diff) {
        object Diff : DiffUtil.ItemCallback<ProgressEntryEntity>() {
            override fun areItemsTheSame(oldItem: ProgressEntryEntity, newItem: ProgressEntryEntity) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: ProgressEntryEntity, newItem: ProgressEntryEntity) = oldItem == newItem
        }
        inner class VH(val item: com.food24.track.databinding.ItemProgressBinding) :
            RecyclerView.ViewHolder(item.root)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val inf = LayoutInflater.from(parent.context)
            val b = com.food24.track.databinding.ItemProgressBinding.inflate(inf, parent, false)
            return VH(b)
        }
        override fun onBindViewHolder(holder: VH, position: Int) {
            val e = getItem(position)
            holder.item.textDate.text = e.date
            holder.item.textWeight.text = "${e.weight} kg"
            holder.item.textCalories.text = "${e.caloriesConsumed} kcal"
        }
    }
}
