package com.food24.track.ui.meals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.food24.track.databinding.FragmentMealDetailBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MealDetailFragment : Fragment() {

    private var _binding: FragmentMealDetailBinding? = null
    private val binding get() = _binding!!

    private val vm: MealDetailViewModel by viewModels()

    private val mealId: Int
        get() = requireArguments().getInt(ARG_MEAL_ID)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMealDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            vm.meal.collectLatest { m ->
                if (m != null) {
                    binding.textTitle.text = m.name
                    binding.textKcal.text = "${m.calories} kcal"
                    binding.textMacros.text = "P:${m.protein}g  F:${m.fat}g  C:${m.carbs}g"
                }
            }
        }
        vm.loadMeal(mealId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_MEAL_ID = "arg_meal_id"
        fun newInstance(mealId: Int) = MealDetailFragment().apply {
            arguments = Bundle().apply { putInt(ARG_MEAL_ID, mealId) }
        }
    }
}
