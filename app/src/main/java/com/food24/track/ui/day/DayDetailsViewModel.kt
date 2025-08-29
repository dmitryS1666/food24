package com.food24.track.ui.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food24.track.data.dao.MealDao
import com.food24.track.data.dao.MealEntryDao
import com.food24.track.data.entity.MealTypes
import kotlinx.coroutines.launch
import com.food24.track.R

class DayDetailsViewModel(
    private val mealEntryDao: MealEntryDao,
    private val mealDao: MealDao
) : ViewModel() {

    private val _sections = kotlinx.coroutines.flow.MutableStateFlow<List<DaySectionUi>>(emptyList())
    val sections: kotlinx.coroutines.flow.StateFlow<List<DaySectionUi>> = _sections

    fun bind(dateIso: String) {
        viewModelScope.launch {
            mealEntryDao.observeEntriesByDate(dateIso).collect { entries ->
                val ids = entries.map { it.mealId }.distinct()
                val meals = if (ids.isEmpty()) emptyList() else mealDao.getByIds(ids)
                val byId = meals.associateBy { it.id }

                val grouped = entries.groupBy { byId[it.mealId]?.type.orEmpty() }

                val list = grouped.map { (type, ents) ->
                    val totalKcal = ents.sumOf { byId[it.mealId]?.calories ?: 0 }
                    val eatenCount = ents.count { it.eaten }
                    val itemsCount = ents.size
                    DaySectionUi(
                        type = type,
                        title = when (type) {
                            MealTypes.BREAKFAST    -> "Breakfast"
                            MealTypes.SNACK        -> "Snack"
                            MealTypes.LUNCH        -> "Lunch"
                            MealTypes.DINNER       -> "Dinner"
                            MealTypes.POST_WORKOUT -> "Post-Workout"
                            else                   -> type.ifBlank { "Other" }
                        },
                        totalKcal = totalKcal,
                        itemsCount = itemsCount,
                        eatenCount = eatenCount,
                        thumbRes = when (type) {
                            MealTypes.BREAKFAST    -> R.drawable.meal_breakfast
                            MealTypes.SNACK        -> R.drawable.meal_snack
                            MealTypes.LUNCH        -> R.drawable.meal_lunch
                            MealTypes.DINNER       -> R.drawable.meal_dinner
                            MealTypes.POST_WORKOUT -> R.drawable.meal_postworkout
                            else                   -> R.drawable.placeholder_meal
                        }
                    )
                }.sortedBy { orderOfType(it.type) }

                _sections.value = list
            }
        }
    }

    private fun orderOfType(type: String) = when (type) {
        MealTypes.BREAKFAST -> 0
        MealTypes.SNACK -> 1
        MealTypes.LUNCH -> 2
        MealTypes.DINNER -> 3
        MealTypes.POST_WORKOUT -> 4
        else -> 99
    }
}
