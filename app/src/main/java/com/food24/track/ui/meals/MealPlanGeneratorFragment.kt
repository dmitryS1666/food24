package com.food24.track.ui.meals

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.food24.track.R
import com.food24.track.databinding.FragmentMealPlanGeneratorBinding
import com.food24.track.ui.home.HomeDashboardFragment
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
        b.spinnerActivity.setAdapterWhite(listOf("Sedentary", "Light", "Moderate", "Active", "Very active"))
        b.spinnerDays.setAdapterWhite(listOf("1 day", "3 days", "7 days", "14 days"))

        // Подписи для кастомных radio-элементов
        b.itemGain.radioLabel.text = "Weight Gain"
        b.itemLoss.radioLabel.text = "Fat Loss"
        b.itemMaintain.radioLabel.text = "Maintain"

        b.itemMeals3.radioLabel.text = "3"
        b.itemMeals4.radioLabel.text = "4"
        b.itemMeals5.radioLabel.text = "5"

        // Назад
        b.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // --- ЛОГИКА ВЫБОРА ЦЕЛИ (эксклюзивно) ---
        fun selectGoal(option: GoalType) {
            // сбрасываем
            b.itemGain.radio.isChecked = false
            b.itemLoss.radio.isChecked = false
            b.itemMaintain.radio.isChecked = false
            // ставим выбранный
            when (option) {
                GoalType.GAIN -> b.itemGain.radio.isChecked = true
                GoalType.LOSS -> b.itemLoss.radio.isChecked = true
                GoalType.MAINTAIN -> b.itemMaintain.radio.isChecked = true
            }
            vm.setGoalType(option)
        }
        // клики по radio и по подписи
        b.itemGain.root.setOnClickListener { selectGoal(GoalType.GAIN) }
        b.itemLoss.root.setOnClickListener { selectGoal(GoalType.LOSS) }
        b.itemMaintain.root.setOnClickListener { selectGoal(GoalType.MAINTAIN) }
        b.itemGain.radio.setOnClickListener { selectGoal(GoalType.GAIN) }
        b.itemLoss.radio.setOnClickListener { selectGoal(GoalType.LOSS) }
        b.itemMaintain.radio.setOnClickListener { selectGoal(GoalType.MAINTAIN) }

        // --- ЛОГИКА ВЫБОРА КОЛ-ВА ПРИЁМОВ ПИЩИ ---
        fun selectMeals(n: Int) {
            b.itemMeals3.radio.isChecked = false
            b.itemMeals4.radio.isChecked = false
            b.itemMeals5.radio.isChecked = false
            when (n) {
                3 -> b.itemMeals3.radio.isChecked = true
                4 -> b.itemMeals4.radio.isChecked = true
                5 -> b.itemMeals5.radio.isChecked = true
            }
            vm.setMealsPerDay(n)
        }
        b.itemMeals3.root.setOnClickListener { selectMeals(3) }
        b.itemMeals4.root.setOnClickListener { selectMeals(4) }
        b.itemMeals5.root.setOnClickListener { selectMeals(5) }
        b.itemMeals3.radio.setOnClickListener { selectMeals(3) }
        b.itemMeals4.radio.setOnClickListener { selectMeals(4) }
        b.itemMeals5.radio.setOnClickListener { selectMeals(5) }

        // Спиннеры
        b.spinnerActivity.setOnItemSelectedListenerCompat { pos ->
            vm.setActivity(b.spinnerActivity.adapter.getItem(pos).toString())
        }
        b.spinnerDays.setOnItemSelectedListenerCompat { pos ->
            val v = when (pos) { 0 -> 1; 1 -> 3; 2 -> 7; else -> 14 }
            vm.setDays(v)
        }

        // Generate
        b.btnGenerate.setOnClickListener {
            val input = validateInputs() ?: return@setOnClickListener

            // прокидываем параметры в VM
            vm.setGoalType(input.goal)
            vm.setMealsPerDay(input.mealsPerDay)
            vm.setActivity(input.activity)
            vm.setDays(input.days)
            vm.setAnthro(input.weight, input.height, input.age)

            setGeneratingUi(true)

            vm.generate(
                startDate = java.time.LocalDate.now(),
                onDone = {
                    setGeneratingUi(false)
                    Toast.makeText(requireContext(), "Plan generated", Toast.LENGTH_SHORT).show()

                    // перейти на главный экран (Dashboard/Home)
                    (activity as? com.food24.track.MainActivity)?.openDashboardFragment()
                    // если хотите убрать экран генератора из back stack:
                    // parentFragmentManager.popBackStack()
                },
                onError = {
                    setGeneratingUi(false)
                    Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_LONG).show()
                }
            )
        }

        // (если нужно подписываться на стейт)
        viewLifecycleOwner.lifecycleScope.launch {
            vm.state.collectLatest { /* обновление UI при необходимости */ }
        }
    }

    private fun Spinner.setAdapterWhite(items: List<String>) {
        adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_selected_item,   // layout выбранного
            R.id.spinnerText,                 // ID TextView внутри layout
            items
        ).apply {
            // для выпадающего списка тоже указываем макет с TextView
            setDropDownViewResource(R.layout.spinner_dropdown_item)
        }
    }

    private data class ValidInput(
        val goal: GoalType,
        val activity: String,
        val weight: Float,
        val height: Int,
        val age: Int,
        val mealsPerDay: Int,
        val days: Int
    )

    private fun MealPlanGeneratorFragment.validateInputs(): ValidInput? {
        // goal
        val goal = when {
            b.itemGain.radio.isChecked -> GoalType.GAIN
            b.itemLoss.radio.isChecked -> GoalType.LOSS
            b.itemMaintain.radio.isChecked -> GoalType.MAINTAIN
            else -> {
                Toast.makeText(requireContext(), "Please select a goal", Toast.LENGTH_SHORT).show()
                b.groupGoal.requestFocus()
                return null
            }
        }

        // activity
        val activity = (b.spinnerActivity.selectedItem?.toString() ?: "").trim()
        if (activity.isEmpty()) {
            Toast.makeText(requireContext(), "Please select activity level", Toast.LENGTH_SHORT).show()
            b.spinnerActivity.performClick()
            return null
        }

        // weight
        val weight = b.editWeight.text.toString().trim().toFloatOrNull()
        if (weight == null || weight <= 0f || weight > 500f) {
            b.editWeight.error = "Enter valid weight"
            b.editWeight.requestFocus()
            return null
        }

        // height
        val height = b.editHeight.text.toString().trim().toIntOrNull()
        if (height == null || height < 50 || height > 260) {
            b.editHeight.error = "Enter valid height (cm)"
            b.editHeight.requestFocus()
            return null
        }

        // age
        val age = b.editAge.text.toString().trim().toIntOrNull()
        if (age == null || age < 5 || age > 110) {
            b.editAge.error = "Enter valid age"
            b.editAge.requestFocus()
            return null
        }

        // meals per day
        val mealsPerDay = when {
            b.itemMeals3.radio.isChecked -> 3
            b.itemMeals4.radio.isChecked -> 4
            b.itemMeals5.radio.isChecked -> 5
            else -> {
                Toast.makeText(requireContext(), "Please select meals per day", Toast.LENGTH_SHORT).show()
                b.groupMeals.requestFocus()
                return null
            }
        }

        // days
        val days = when (b.spinnerDays.selectedItemPosition) {
            0 -> 1
            1 -> 3
            2 -> 7
            3 -> 14
            else -> 1
        }

        return ValidInput(goal, activity, weight, height, age, mealsPerDay, days)
    }

    private fun setGeneratingUi(isGenerating: Boolean) {
        b.btnGenerate.isEnabled = !isGenerating
        b.btnGenerate.alpha = if (isGenerating) 0.6f else 1f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}

/** маленький helper для Spinner */
private fun Spinner.setOnItemSelectedListenerCompat(onSelected: (position: Int) -> Unit) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>, view: View?, position: Int, id: Long
        ) { onSelected(position) }
        override fun onNothingSelected(parent: AdapterView<*>) {}
    }
}
