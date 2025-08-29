package com.food24.track.ui.day

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.food24.track.R
import com.food24.track.databinding.FragmentDayDetailsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DayDetailsFragment : Fragment() {

    private var _b: FragmentDayDetailsBinding? = null
    private val b get() = _b!!

    private val vm: DayDetailsViewModel by viewModels {
        val app = requireActivity().application as com.food24.track.App
        DayDetailsVMFactory(app.db.mealEntryDao(), app.db.mealDao())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private var date: LocalDate = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    private val adapter = DaySectionsAdapter { type ->
        // Переход к списку блюд выбранной секции (Breakfast/Snack/...)
        parentFragmentManager.beginTransaction()
            .replace(
                R.id.mainFragmentContainer,
                MealSectionFragment.newInstance(date.toString(), type)
            )
            .addToBackStack(null)
            .commit()
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentDayDetailsBinding.inflate(i, c, false)
        return b.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.recycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        b.recycler.adapter = adapter

        arguments?.getString("date")?.let { date = LocalDate.parse(it) }
        vm.bind(date.toString())

        val fmt = DateTimeFormatter.ofPattern("EEEE, MMM d")
        b.textDay.text = date.format(fmt)

        b.btnPrev.setOnClickListener {
            date = date.minusDays(1)
            b.textDay.text = date.format(fmt)
            vm.bind(date.toString())
        }
        b.btnNext.setOnClickListener {
            date = date.plusDays(1)
            b.textDay.text = date.format(fmt)
            vm.bind(date.toString())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.sections.collectLatest { sections -> adapter.submit(sections) }
        }

        b.btnRegenerate.setOnClickListener {
            val dateIso = date.toString()
            vm.regenerateDay(
                dateIso = dateIso,
                mealsPerDay = 4,   // при желании подставь реальное значение из Goal/настроек
                onDone = {
                    Toast.makeText(requireContext(), "Day regenerated", Toast.LENGTH_SHORT).show()
                    vm.bind(dateIso) // если подписка по flow — можно не звать
                },
                onError = { t ->
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }

    companion object {
        fun newInstance(dateIso: String) = DayDetailsFragment().apply {
            arguments = Bundle().apply { putString("date", dateIso) }
        }
    }
}
