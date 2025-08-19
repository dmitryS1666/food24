package com.food24.track.ui.meals

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.food24.track.databinding.FragmentMealDetailBinding
import com.food24.track.databinding.ItemMealDetailFoodBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MealDetailFragment : Fragment() {

    private var _b: FragmentMealDetailBinding? = null
    private val b get() = _b!!

    private val vm: MealDetailViewModel by viewModels {
        val app = requireActivity().application as com.food24.track.App
        MealDetailViewModelFactory(app.db.mealDao(), app.db.mealEntryDao())
    }

    private val adapter = FoodAdapter(
        onSwap = { vm.swap(it) },
        onDelete = { vm.delete(it) }
    )

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentMealDetailBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.recyclerFoods.adapter = adapter

        val date = requireArguments().getString(ARG_DATE) ?: java.time.LocalDate.now().toString()
        val type = requireArguments().getString(ARG_TYPE) ?: "Lunch"
        vm.init(date, type)
        b.textMealTitle.text = type

        viewLifecycleOwner.lifecycleScope.launch {
            vm.ui.collectLatest { st ->
                b.textTotalKcal.text = "${st.totalKcal} kcal"
                b.textTotalsTitle.text = "Total: ${st.totalKcal} kcal"
                adapter.submit(st.items)

                // если есть цели по БЖУ на приём — можно подставить, пока 0/заглушка
                b.textProteinCount.text = "${st.protein}/0"
                b.textFatCount.text = "${st.fat}/0"
                b.textCarbCount.text = "${st.carbs}/0"
            }
        }

        b.btnAddItem.setOnClickListener {
            // TODO: открытие выбора блюда по типу (пока добавим рандом)
            vm.swap(-1) // простая замена-заглушка
        }

        b.btnSave.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }

    companion object {
        const val ARG_DATE = "arg_date"
        const val ARG_TYPE = "arg_type"
    }
}

/* фабрика VM */
class MealDetailViewModelFactory(
    private val mealDao: com.food24.track.data.dao.MealDao,
    private val mealEntryDao: com.food24.track.data.dao.MealEntryDao
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MealDetailViewModel(mealDao, mealEntryDao) as T
        }
        throw IllegalArgumentException("Unknown VM")
    }
}

/* адаптер */
private class FoodAdapter(
    val onSwap: (mealId: Int) -> Unit,
    val onDelete: (mealId: Int) -> Unit
) : RecyclerView.Adapter<FoodVH>() {

    private val items = mutableListOf<UiFood>()
    fun submit(newItems: List<UiFood>) { items.clear(); items.addAll(newItems); notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodVH {
        val b = ItemMealDetailFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodVH(b)
    }
    override fun onBindViewHolder(h: FoodVH, position: Int) {
        val it = items[position]
        with(h.b) {
            textTitle.text = it.title
            textKcal.text = "${it.kcal} kcal"
            textMacros.text = "P: ${it.p} g   F: ${it.f} g   C: ${it.c} g"
            btnSwap.setOnClickListener { onSwap(it.id) }
            btnDelete.setOnClickListener { onDelete(it.id) }
        }
    }
    override fun getItemCount() = items.size
}
private class FoodVH(val b: ItemMealDetailFoodBinding) : RecyclerView.ViewHolder(b.root)
