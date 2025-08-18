package com.food24.track.ui.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.food24.track.data.entity.GoalEntity
import com.food24.track.databinding.FragmentGoalsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GoalsFragment : Fragment() {

    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!

    private val vm: GoalsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            vm.goal.collectLatest { g ->
                if (g != null) {
                    binding.editCurrentWeight.setText(g.currentWeight.toString())
                    binding.editTargetWeight.setText(g.targetWeight.toString())
                    binding.editDailyCalories.setText(g.dailyCalories.toString())
                    binding.editMealsPerDay.setText(g.mealsPerDay.toString())
                    binding.editWeeklyGoal.setText(g.weeklyGoal.toString())
                    binding.editTimeframe.setText(g.timeframeWeeks.toString())
                }
            }
        }

        binding.btnSave.setOnClickListener {
            val goal = GoalEntity(
                id = 1,
                currentWeight = binding.editCurrentWeight.text.toString().toFloatOrNull() ?: 70f,
                targetWeight = binding.editTargetWeight.text.toString().toFloatOrNull() ?: 68f,
                weeklyGoal = binding.editWeeklyGoal.text.toString().toFloatOrNull() ?: -0.5f,
                timeframeWeeks = binding.editTimeframe.text.toString().toIntOrNull() ?: 6,
                dailyCalories = binding.editDailyCalories.text.toString().toIntOrNull() ?: 1800,
                mealsPerDay = binding.editMealsPerDay.text.toString().toIntOrNull() ?: 5,
                goalType = "Lose"
            )
            vm.saveGoal(goal)
        }

        vm.loadGoal()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
