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
import com.food24.track.data.entity.MealTypes
import com.food24.track.databinding.FragmentMealSectionBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MealSectionFragment : Fragment() {

    private var _b: FragmentMealSectionBinding? = null
    private val b get() = _b!!

    private val vm: MealSectionViewModel by viewModels {
        val app = requireActivity().application as com.food24.track.App
        MealSectionVMFactory(app.db.mealEntryDao(), app.db.mealDao())
    }

    private val adapter by lazy {
        MealSectionAdapter { entryId, eaten ->
            vm.toggle(entryId, eaten)   // только (Int, Boolean) -> Unit
        }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentMealSectionBinding.inflate(i, c, false)
        return b.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.list.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        b.list.adapter = adapter

        val date = requireArguments().getString(ARG_DATE)!!
        val type = requireArguments().getString(ARG_TYPE)!!

        val fmt = DateTimeFormatter.ofPattern("EEE, MMM d")
        b.textHeader.text = "${typeToTitle(type)} • " + LocalDate.parse(date).format(fmt)

        vm.bind(date, type)

        viewLifecycleOwner.lifecycleScope.launch {
            vm.items.collectLatest { adapter.submit(it) }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }

    private fun typeToTitle(t: String) = when (t) {
        MealTypes.BREAKFAST    -> "Breakfast"
        MealTypes.SNACK        -> "Snack"
        MealTypes.LUNCH        -> "Lunch"
        MealTypes.DINNER       -> "Dinner"
        MealTypes.POST_WORKOUT -> "Post-Workout"
        else -> t
    }

    companion object {
        private const val ARG_DATE = "date"
        private const val ARG_TYPE = "type"
        fun newInstance(dateIso: String, type: String) = MealSectionFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_DATE, dateIso)
                putString(ARG_TYPE, type)
            }
        }
    }
}
