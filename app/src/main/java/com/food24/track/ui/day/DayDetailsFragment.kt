package com.food24.track.ui.day

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.food24.track.databinding.FragmentDayDetailsBinding
import com.food24.track.databinding.ItemDayMealBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DayDetailsFragment : Fragment() {

    private var _b: FragmentDayDetailsBinding? = null
    private val b get() = _b!!

    private val vm: DayDetailsViewModel by viewModels {
        DayDetailsVMFactory.from(requireActivity().application)
    }

    private val adapter = DayMealAdapter(
        onToggle = { id, eaten -> vm.setEaten(id, eaten) }
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private val niceFmt = DateTimeFormatter.ofPattern("EEEE, MMMM d")

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentDayDetailsBinding.inflate(i, c, false)
        return b.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.recycler.layoutManager = LinearLayoutManager(requireContext())
        b.recycler.adapter = adapter
        b.recycler.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )

        val dateIso = requireArguments().getString(ARG_DATE)!!
        vm.bind(dateIso)

        // заголовок
        val d = LocalDate.parse(dateIso)
        b.textDay.text = d.format(niceFmt)

        b.btnPrev.setOnClickListener {
            val prev = d.minusDays(1)
            open(prev)
        }
        b.btnNext.setOnClickListener {
            val next = d.plusDays(1)
            open(next)
        }

        b.btnRegenerate.setOnClickListener {
            // тут можно дернуть твой генератор на 1 день (по желанию)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.items.collectLatest { adapter.submit(it) }
        }
    }

    private fun open(date: LocalDate) {
        parentFragmentManager.beginTransaction()
            .replace(id, newInstance(date.toString()))
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }

    companion object {
        private const val ARG_DATE = "arg_date"
        fun newInstance(dateIso: String) = DayDetailsFragment().apply {
            arguments = Bundle().apply { putString(ARG_DATE, dateIso) }
        }
    }
}

/* --- адаптер списка блюд дня --- */
private class DayMealAdapter(
    val onToggle: (mealId: Int, eaten: Boolean) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<DayMealVH>() {

    private val items = mutableListOf<DayMealUi>()

    fun submit(newItems: List<DayMealUi>) {
        items.clear(); items.addAll(newItems); notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int): DayMealVH {
        val b = ItemDayMealBinding.inflate(LayoutInflater.from(p.context), p, false)
        return DayMealVH(b)
    }

    override fun onBindViewHolder(h: DayMealVH, pos: Int) {
        val it = items[pos]
        with(h.b) {
            textTitle.text = it.title
            textKcal.text = "${it.calories} kcal"
            checkEaten.isChecked = it.eaten

            // простая картинка по типу – можно переиспользовать маппинг из Home
            img.setImageResource(
                when (it.type.lowercase()) {
                    "breakfast"    -> com.food24.track.R.drawable.meal_breakfast
                    "snack"        -> com.food24.track.R.drawable.meal_snack
                    "lunch"        -> com.food24.track.R.drawable.meal_lunch
                    "dinner"       -> com.food24.track.R.drawable.meal_dinner
                    "postworkout", "post-workout", "post_workout" ->
                        com.food24.track.R.drawable.meal_postworkout
                    else           -> com.food24.track.R.drawable.placeholder_meal
                }
            )

            checkEaten.setOnCheckedChangeListener { _, checked ->
                onToggle(it.mealId, checked)
            }
        }
    }

    override fun getItemCount() = items.size
}

private class DayMealVH(val b: ItemDayMealBinding) :
    androidx.recyclerview.widget.RecyclerView.ViewHolder(b.root)
