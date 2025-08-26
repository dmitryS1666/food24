package com.food24.track.ui.home

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.food24.track.R
import com.food24.track.databinding.FragmentHomeDashboardBinding
import com.food24.track.databinding.ItemHomeMealCardBinding
import com.food24.track.ui.goals.GoalsFragment
import com.food24.track.ui.meals.MealPlanGeneratorFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.math.roundToInt
import android.view.View
import com.food24.track.data.entity.MealTypes
import com.food24.track.ui.day.DayDetailsFragment
import java.text.NumberFormat
import java.util.Locale

class HomeDashboardFragment : Fragment() {

    private var _b: FragmentHomeDashboardBinding? = null
    private val b get() = _b!!

    private val vm: HomeDashboardViewModel by viewModels {
        val app = requireActivity().application as com.food24.track.App
        HomeDashboardViewModelFactory(
            app.db.goalDao(), app.db.dailyPlanDao(), app.db.mealEntryDao(), app.db.mealDao()
        )
    }

    private val adapter = MealCardAdapter { /* TODO: open details */ }

    @RequiresApi(Build.VERSION_CODES.O)
    private var currentDate: LocalDate = LocalDate.now()
    @RequiresApi(Build.VERSION_CODES.O)
    private val uiFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    @RequiresApi(Build.VERSION_CODES.O)
    private val dbFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentHomeDashboardBinding.inflate(i, c, false)

        b.recyclerMealsGrid.layoutManager = GridLayoutManager(requireContext(), 2)
        b.recyclerMealsGrid.adapter = adapter
        if (b.recyclerMealsGrid.itemDecorationCount == 0) {
            b.recyclerMealsGrid.addItemDecoration(gridSpacing(3))
        }

        b.cardDate.setOnClickListener {
            val dateIso = currentDate.format(dbFormatter)
            parentFragmentManager.beginTransaction()
                .replace(R.id.mainFragmentContainer, DayDetailsFragment.newInstance(dateIso))
                .addToBackStack(null)
                .commit()
        }

        b.btnNewPlan.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.mainFragmentContainer, MealPlanGeneratorFragment())
                .addToBackStack(null)
                .commit()
        }

        b.cardGoal.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.mainFragmentContainer, GoalsFragment())
                .addToBackStack(null)
                .commit()
        }

        return b.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        renderDate()
        vm.bind(currentDate.format(dbFormatter))

        b.btnPickDate.setOnClickListener { showDatePicker() }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.ui.collectLatest { st ->
                b.textGoal.text = st.goalTitle
                b.textTargetRange.text = st.targetRange

                b.textCalories.text = "${st.consumed} / ${st.target} kcal"
                b.progressProtein.progress = pct(st.protein.first, st.protein.second)
                b.progressFat.progress     = pct(st.fat.first,     st.fat.second)
                b.progressCarb.progress    = pct(st.carbs.first,   st.carbs.second)

                b.textProteinCount.text = "${st.protein.first}/${st.protein.second}"
                b.textFatCount.text     = "${st.fat.first}/${st.fat.second}"
                b.textCarbCount.text    = "${st.carbs.first}/${st.carbs.second}"

                adapter.submit(st.mealCards)
            }
        }
    }

    private fun gridSpacing(spaceDp: Int): RecyclerView.ItemDecoration {
        val px = (resources.displayMetrics.density * spaceDp).toInt()
        return object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: android.graphics.Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.set(px, px, px, px)
            }
        }
    }

    private fun pct(v: Int, target: Int): Int =
        if (target <= 0) 0 else ((v * 100f / target).roundToInt()).coerceIn(0, 100)

    @RequiresApi(Build.VERSION_CODES.O)
    private fun renderDate() {
        b.textDate.text = currentDate.format(uiFormatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDatePicker() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentDate.year)
            set(Calendar.MONTH, currentDate.monthValue - 1)
            set(Calendar.DAY_OF_MONTH, currentDate.dayOfMonth)
        }
        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                currentDate = LocalDate.of(y, m + 1, d)
                renderDate()
                vm.bind(currentDate.format(dbFormatter))   // было vm.load(...)
            },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView(); _b = null
    }
}

/* --- простой адаптер карточек --- */
private class MealCardAdapter(
    val onClick: (Int) -> Unit
) : RecyclerView.Adapter<MealCardVH>() {

    private val items = mutableListOf<MealCardUi>()

    fun submit(newItems: List<MealCardUi>) {
        items.clear(); items.addAll(newItems); notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealCardVH {
        val b = ItemHomeMealCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MealCardVH(b)
    }

    override fun onBindViewHolder(h: MealCardVH, position: Int) {
        val it = items[position]
        with(h.b) {
            textType.text = it.type
            textKcal.text = "${it.calories}kcal"

            val imgRes = when (it.type) {
                MealTypes.BREAKFAST    -> R.drawable.meal_breakfast
                MealTypes.SNACK        -> R.drawable.meal_snack
                MealTypes.LUNCH        -> R.drawable.meal_lunch
                MealTypes.DINNER       -> R.drawable.meal_dinner
                MealTypes.POST_WORKOUT -> R.drawable.meal_postworkout
                else                   -> R.drawable.placeholder_meal
            }
            img.setImageResource(imgRes)

            overlayDim.visibility = if (it.eaten) View.GONE else View.VISIBLE

            root.setOnClickListener { onClick(it.id) }
            btnView.setOnClickListener { onClick(it.id) }
            btnEdit.setOnClickListener { onClick(it.id) }
        }
    }


    override fun getItemCount() = items.size
}

private class MealCardVH(val b: ItemHomeMealCardBinding) : RecyclerView.ViewHolder(b.root)
