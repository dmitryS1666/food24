package com.food24.track.ui.progress

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.food24.track.databinding.FragmentProgressBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

class ProgressFragment : Fragment() {

    private var _b: FragmentProgressBinding? = null
    private val b get() = _b!!

    private val vm: ProgressViewModel by viewModels {
        val app = requireActivity().application as com.food24.track.App
        ProgressVMFactory(app.db.weightLogDao(), app.db.goalDao(), app.db.dailyPlanDao(), app.db.mealEntryDao())
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentProgressBinding.inflate(i, c, false); return b.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        b.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // добавить вес
        b.actionAddWeight.setOnClickListener {
            val now = LocalDate.now()
            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    val date = LocalDate.of(y, m + 1, d)
                    // простой инпут веса
                    val dlg = android.app.AlertDialog.Builder(requireContext())
                    val input = android.widget.EditText(requireContext()).apply {
                        inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                        hint = "Weight, kg"
                        setPadding(32, 24, 32, 24)
                        setText("")
                    }
                    dlg.setTitle("Add Weight")
                        .setView(input)
                        .setPositiveButton("Save") { _, _ ->
                            val v = input.text.toString().toFloatOrNull()
                            if (v != null && v in 20f..500f) vm.addWeight(date, v)
                            else Toast.makeText(requireContext(), "Enter valid weight", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                },
                now.year, now.monthValue - 1, now.dayOfMonth
            ).show()
        }

        b.btnReset.setOnClickListener { vm.reset() }

        // tabs
        var showMonth = false
        fun renderBars(ui: ProgressUi) {
            val days = if (showMonth) 30 else 7
            val bars = ui.bars.takeLast(days)
            b.calorieChart.setBars(bars, ui.targetLow, ui.targetHigh)
        }
        b.tabWeek.setOnClickListener { showMonth = false; vm.ui.value.let { renderBars(it) } }
        b.tabMonth.setOnClickListener { showMonth = true; vm.ui.value.let { renderBars(it) } }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.ui.collectLatest { ui ->
                // карточка веса
                b.textCurrentWeight.text = "Current weight: ${ui.current?.let { "%.1f".format(it) } ?: "—"} kg"
                b.textTargetWeight.text = "Target weight: ${ui.target?.let { "%.1f".format(it) } ?: "—"} kg"
                b.textProgress.text = ui.progressText

                // график веса
                val pts = ui.weights.mapIndexed { idx, pair ->
                    com.food24.track.ui.progress.LineChartView.Point(idx.toFloat(), pair.second)
                }
                b.weightChart.setPoints(pts)

                // калории
                b.textCalGoal.text = "Goal: ${ui.goalKcal} kcal/day"
                b.textCalAvg.text = "Average intake (7 days): ${ui.avg7days} kcal"
                b.textCalHits.text = "Hit target: ${ui.hits7days} out of 7 days"

                renderBars(ui)
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
