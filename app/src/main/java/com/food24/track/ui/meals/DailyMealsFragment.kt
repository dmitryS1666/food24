package com.food24.track.ui.meals

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
import com.food24.track.databinding.FragmentDailyMealsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DailyMealsFragment : Fragment() {

    private var _binding: FragmentDailyMealsBinding? = null
    private val binding get() = _binding!!

    private val vm: DailyMealsViewModel by viewModels()
    private val adapter = MealsAdapter(
        onToggleEaten = { meal -> vm.markEaten(currentDate, meal.id) }
    )

    private val currentDate: String
        get() = arguments?.getString(ARG_DATE) ?: DEFAULT_DATE

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDailyMealsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerMeals.adapter = adapter
        binding.textDate.text = currentDate

        viewLifecycleOwner.lifecycleScope.launch {
            vm.uiState.collectLatest { state ->
                binding.emptyView.isVisible = state.meals.isEmpty()
                adapter.submitList(state.meals)
            }
        }

        vm.loadMeals(currentDate)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class MealsAdapter(
        private val onToggleEaten: (UiMeal) -> Unit
    ) : ListAdapter<UiMeal, MealsAdapter.VH>(Diff) {

        object Diff : DiffUtil.ItemCallback<UiMeal>() {
            override fun areItemsTheSame(oldItem: UiMeal, newItem: UiMeal) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: UiMeal, newItem: UiMeal) = oldItem == newItem
        }

        inner class VH(val item: com.food24.track.databinding.ItemDailyMealBinding) :
            RecyclerView.ViewHolder(item.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val inf = LayoutInflater.from(parent.context)
            val binding = com.food24.track.databinding.ItemDailyMealBinding.inflate(inf, parent, false)
            return VH(binding)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val meal = getItem(position)
            with(holder.item) {
                textMealName.text = meal.name
                textCalories.text = "${meal.calories} kcal"
                checkEaten.isChecked = meal.eaten
                checkEaten.setOnClickListener { onToggleEaten(meal) }
            }
        }
    }

    companion object {
        private const val ARG_DATE = "arg_date"
        private const val DEFAULT_DATE = "2025-08-18"
        fun newInstance(date: String) = DailyMealsFragment().apply {
            arguments = Bundle().apply { putString(ARG_DATE, date) }
        }
    }
}
