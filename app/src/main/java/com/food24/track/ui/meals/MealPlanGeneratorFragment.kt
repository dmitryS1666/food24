package com.food24.track.ui.meals

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.food24.track.databinding.FragmentMealPlanGeneratorBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

class MealPlanGeneratorFragment : Fragment() {

    private var _b: FragmentMealPlanGeneratorBinding? = null
    private val b get() = _b!!

    private val vm: MealPlanViewModel by viewModels {
        MealPlanViewModelFactory.from(requireActivity().application)
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentMealPlanGeneratorBinding.inflate(i, c, false)
        return b.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Спиннеры
        b.spinnerActivity.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Sedentary", "Light", "Moderate", "Active", "Very active")
        )
        b.spinnerDays.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listOf("1 day", "3 days", "7 days", "14 days")
        )

        // Goal (RadioGroup)
        b.groupGoal.setOnCheckedChangeListener { _, id ->
            vm.setGoalType(
                when (id) {
                    b.rbGain.id -> GoalType.GAIN
                    b.rbMaintain.id -> GoalType.MAINTAIN
                    else -> GoalType.LOSS
                }
            )
        }
        // Meals per day
        b.groupMeals.setOnCheckedChangeListener { _, id ->
            vm.setMealsPerDay(
                when (id) {
                    b.rbMeals3.id -> 3
                    b.rbMeals4.id -> 4
                    else -> 5
                }
            )
        }
        // Activity + days
        b.spinnerActivity.setOnItemSelectedListenerCompat { pos ->
            vm.setActivity(b.spinnerActivity.adapter.getItem(pos).toString())
        }
        b.spinnerDays.setOnItemSelectedListenerCompat { pos ->
            val v = when (pos) { 0 -> 1; 1 -> 3; 2 -> 7; else -> 14 }
            vm.setDays(v)
        }

        // Кнопка "Generate"
        b.btnGenerate.setOnClickListener {
            val weight = b.editWeight.text.toString().toFloatOrNull()
            val height = b.editHeight.text.toString().toIntOrNull()
            val age = b.editAge.text.toString().toIntOrNull()
            vm.setAnthro(weight, height, age)

            vm.generate(
                startDate = LocalDate.now(),
                onDone = { Toast.makeText(requireContext(), "Plan generated", Toast.LENGTH_SHORT).show() },
                onError = { Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_LONG).show() }
            )
        }

        // (опционально) подписка на стейт
        viewLifecycleOwner.lifecycleScope.launch {
            vm.state.collectLatest { /* при необходимости обновляй UI */ }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

/* маленький helper для Spinner без лишнего кода */
private fun android.widget.Spinner.setOnItemSelectedListenerCompat(onSelected: (position: Int) -> Unit) {
    onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
            onSelected(position)
        }
        override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
    }
}
