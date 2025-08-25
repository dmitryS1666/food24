package com.food24.track.ui.goals

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.food24.track.App
import com.food24.track.databinding.FragmentGoalsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GoalsFragment : Fragment() {

    private var _b: FragmentGoalsBinding? = null
    private val b get() = _b!!

    private val vm: GoalsViewModel by viewModels {
        val app = requireActivity().application as App
        GoalsViewModelFactory(app.db.goalDao())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentGoalsBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // подписи для кастомных радио-айтемов
        b.itemGain.radioLabel.text = "Weight Gain"
        b.itemLoss.radioLabel.text = "Fat Loss"
        b.itemMaintain.radioLabel.text = "Maintain"

        b.itemMeals3.radioLabel.text = "3"
        b.itemMeals4.radioLabel.text = "4"
        b.itemMeals5.radioLabel.text = "5"

        // текст-вотчеры
        b.editWeight.addFloatWatcher { vm.setWeight(it) }
        b.editTargetWeight.addFloatWatcher { vm.setTargetWeight(it) }
        b.editWeeklyGoal.addFloatWatcher { vm.setWeeklyGoal(it) }
        b.editTimeframe.addIntWatcher { vm.setTimeframe(it) }

        // Назад
        b.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // --- Эксклюзивный выбор цели ---
        fun selectGoal(type: GoalType) {
            b.itemGain.radio.isChecked = false
            b.itemLoss.radio.isChecked = false
            b.itemMaintain.radio.isChecked = false
            when (type) {
                GoalType.GAIN -> b.itemGain.radio.isChecked = true
                GoalType.LOSS -> b.itemLoss.radio.isChecked = true
                GoalType.MAINTAIN -> b.itemMaintain.radio.isChecked = true
            }
            vm.setGoalType(type)
        }
        b.itemGain.root.setOnClickListener { selectGoal(GoalType.GAIN) }
        b.itemLoss.root.setOnClickListener { selectGoal(GoalType.LOSS) }
        b.itemMaintain.root.setOnClickListener { selectGoal(GoalType.MAINTAIN) }
        b.itemGain.radio.setOnClickListener { selectGoal(GoalType.GAIN) }
        b.itemLoss.radio.setOnClickListener { selectGoal(GoalType.LOSS) }
        b.itemMaintain.radio.setOnClickListener { selectGoal(GoalType.MAINTAIN) }

        // --- Эксклюзивный выбор кол-ва приёмов пищи ---
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

        // Сохранить цель
        b.btnSaveGoal.setOnClickListener {
            vm.save(
                onDone = {
                    Toast.makeText(requireContext(), "Goal saved", Toast.LENGTH_SHORT).show()
                    (activity as? com.food24.track.MainActivity)?.openDashboardFragment()
                },
                onError = { Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_LONG).show() }
            )
        }
        b.actionReset.setOnClickListener { vm.resetToDefault() }

        // Подписка на стейт
        viewLifecycleOwner.lifecycleScope.launch {
            vm.ui.collectLatest { st ->
                // поля
                setTextIfChanged(b.editWeight,        st.weight?.toString().orEmpty())
                setTextIfChanged(b.editTargetWeight,  st.targetWeight?.toString().orEmpty())
                setTextIfChanged(b.editWeeklyGoal,    st.weeklyGoalKg?.toString().orEmpty())
                setTextIfChanged(b.editTimeframe,     st.timeframeWeeks?.toString().orEmpty())

                // радио
                when (st.goalType) {
                    GoalType.GAIN      -> selectGoal(GoalType.GAIN)
                    GoalType.LOSS      -> selectGoal(GoalType.LOSS)
                    GoalType.MAINTAIN  -> selectGoal(GoalType.MAINTAIN)
                }
                selectMeals(st.mealsPerDay ?: 3)

                // расчёты
                b.textMaintenance.text = "Maintenance: ${st.maintenance} kcal"
                b.textDailyTarget.text = "Your Daily Target: ${st.dailyTarget} kcal"
            }
        }

        vm.load()
    }

    private fun setTextIfChanged(v: EditText, value: String) {
        if (v.text?.toString() != value) v.setText(value)
    }

    override fun onDestroyView() {
        super.onDestroyView(); _b = null
    }
}

/* --- helpers --- */
private fun EditText.addFloatWatcher(onValue: (Float?) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) { onValue(s?.toString()?.toFloatOrNull()) }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}
private fun EditText.addIntWatcher(onValue: (Int?) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) { onValue(s?.toString()?.toIntOrNull()) }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}
