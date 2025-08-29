package com.food24.track.ui.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food24.track.data.dao.MealDao
import com.food24.track.data.dao.MealEntryDao
import com.food24.track.data.entity.MealTypes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.food24.track.R

class DayDetailsViewModel(
    private val mealEntryDao: MealEntryDao,
    private val mealDao: MealDao
) : ViewModel() {

    private val _sections = MutableStateFlow<List<DaySectionUi>>(emptyList())
    val sections: StateFlow<List<DaySectionUi>> = _sections

    fun bind(dateIso: String) {
        viewModelScope.launch {
            mealEntryDao.observeEntriesByDate(dateIso).collectLatest { entries ->
                val ids = entries.map { it.mealId }.distinct()
                val meals = if (ids.isEmpty()) emptyList() else mealDao.getByIds(ids)
                val byId = meals.associateBy { it.id }

                // сгруппировали приёмы по типам блюд
                val groups = entries.groupBy { byId[it.mealId]?.type ?: "Other" }

                val order = listOf(
                    MealTypes.BREAKFAST,
                    MealTypes.SNACK,
                    MealTypes.LUNCH,
                    MealTypes.DINNER,
                    MealTypes.POST_WORKOUT
                )

                val result = groups.map { (type, ents) ->
                    val totalKcal = ents.sumOf { e -> byId[e.mealId]?.calories ?: 0 }
                    val itemsCount = ents.size
                    val eatenCount = ents.count { it.eaten }

                    DaySectionUi(
                        type = type,
                        title = typeToTitle(type),
                        totalKcal = totalKcal,
                        itemsCount = itemsCount,
                        eatenCount = eatenCount,
                        thumbRes = typeThumb(type)
                    )
                }.sortedBy { t ->
                    val idx = order.indexOf(t.type)
                    if (idx == -1) Int.MAX_VALUE else idx
                }

                _sections.value = result
            }
        }
    }

    private fun typeToTitle(t: String) = when (t) {
        MealTypes.BREAKFAST    -> "Breakfast"
        MealTypes.SNACK        -> "Snack"
        MealTypes.LUNCH        -> "Lunch"
        MealTypes.DINNER       -> "Dinner"
        MealTypes.POST_WORKOUT -> "Post-Workout"
        else                   -> t
    }

    private fun typeThumb(t: String) = when (t) {
        MealTypes.BREAKFAST    -> R.drawable.meal_breakfast
        MealTypes.SNACK        -> R.drawable.meal_snack
        MealTypes.LUNCH        -> R.drawable.meal_lunch
        MealTypes.DINNER       -> R.drawable.meal_dinner
        MealTypes.POST_WORKOUT -> R.drawable.meal_postworkout
        else                   -> R.drawable.placeholder_meal
    }
}
