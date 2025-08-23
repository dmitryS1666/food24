package com.food24.track.ui.goals

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.RadioButton
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

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentGoalsBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // watchers
        b.editWeight.addFloatWatcher { vm.setWeight(it) }
        b.editTargetWeight.addFloatWatcher { vm.setTargetWeight(it) }
        b.editWeeklyGoal.addFloatWatcher { vm.setWeeklyGoal(it) }
        b.editTimeframe.addIntWatcher { vm.setTimeframe(it) }

        b.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        b.groupGoalType.setOnCheckedChangeListener { _, id ->
            vm.setGoalType(
                when (id) {
                    b.rbGain.id -> GoalType.GAIN
                    b.rbMaintain.id -> GoalType.MAINTAIN
                    else -> GoalType.LOSS
                }
            )
        }
        b.groupMealsPerDay.setOnCheckedChangeListener { _, id ->
            vm.setMealsPerDay(
                when (id) {
                    b.rbMeals3.id -> 3
                    b.rbMeals4.id -> 4
                    else -> 5
                }
            )
        }

        b.btnSaveGoal.setOnClickListener {
            vm.save(
                onDone = { Toast.makeText(requireContext(), "Goal saved", Toast.LENGTH_SHORT).show() },
                onError = { Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_LONG).show() }
            )
        }
        b.actionReset.setOnClickListener { vm.resetToDefault() }

        // observe
        viewLifecycleOwner.lifecycleScope.launch {
            vm.ui.collectLatest { st ->
                // заполнение полей (без зацикливания watchers)
                setTextIfChanged(b.editWeight, st.weight?.toString().orEmpty())
                setTextIfChanged(b.editTargetWeight, st.targetWeight?.toString().orEmpty())
                setTextIfChanged(b.editWeeklyGoal, st.weeklyGoalKg?.toString().orEmpty())
                setTextIfChanged(b.editTimeframe, st.timeframeWeeks?.toString().orEmpty())

                checkGoalRadio(st.goalType)
                checkMealsRadio(st.mealsPerDay)

                b.textMaintenance.text = "Maintenance: ${st.maintenance} kcal"
                b.textDailyTarget.text = "Your Daily Target: ${st.dailyTarget} kcal"
            }
        }

        vm.load()
    }

    private fun checkGoalRadio(type: GoalType) {
        val rb: RadioButton = when (type) {
            GoalType.GAIN -> b.rbGain
            GoalType.MAINTAIN -> b.rbMaintain
            GoalType.LOSS -> b.rbLoss
        }
        if (b.groupGoalType.checkedRadioButtonId != rb.id) rb.isChecked = true
    }

    private fun checkMealsRadio(n: Int) {
        val id = when (n) { 3 -> b.rbMeals3.id; 4 -> b.rbMeals4.id; else -> b.rbMeals5.id }
        if (b.groupMealsPerDay.checkedRadioButtonId != id) b.groupMealsPerDay.check(id)
    }

    private fun setTextIfChanged(v: android.widget.EditText, value: String) {
        if (v.text?.toString() != value) { v.setText(value) }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

/* helpers */
private fun android.widget.EditText.addFloatWatcher(onValue: (Float?) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) { onValue(s?.toString()?.toFloatOrNull()) }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}
private fun android.widget.EditText.addIntWatcher(onValue: (Int?) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) { onValue(s?.toString()?.toIntOrNull()) }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}
