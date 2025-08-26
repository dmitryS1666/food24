package com.food24.track.ui.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food24.track.data.dao.MealDao
import com.food24.track.data.dao.MealEntryDao
import com.food24.track.data.entity.MealEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class DayMealUi(
    val mealId: Int,
    val title: String,
    val calories: Int,
    val type: String,
    val eaten: Boolean
)

class DayDetailsViewModel(
    private val mealEntryDao: MealEntryDao,
    private val mealDao: MealDao
) : ViewModel() {

    private val _items = MutableStateFlow<List<DayMealUi>>(emptyList())
    val items: StateFlow<List<DayMealUi>> = _items

    private val _date = MutableStateFlow("")
    val date: StateFlow<String> = _date

    fun bind(dateIso: String) {
        if (_date.value == dateIso) return
        _date.value = dateIso

        viewModelScope.launch {
            mealEntryDao.observeEntriesByDate(dateIso).collect { entries ->
                val ids = entries.map { it.mealId }.distinct()
                val meals: List<MealEntity> =
                    if (ids.isEmpty()) emptyList() else mealDao.getByIds(ids)
                val byId = meals.associateBy { it.id }

                val ui = entries.mapNotNull { e ->
                    val m = byId[e.mealId] ?: return@mapNotNull null
                    DayMealUi(
                        mealId = m.id,
                        title = m.name,
                        calories = m.calories,
                        type = m.type,
                        eaten = e.eaten
                    )
                }
                _items.value = ui
            }
        }
    }

    fun setEaten(mealId: Int, eaten: Boolean) {
        val d = _date.value
        viewModelScope.launch { mealEntryDao.setEaten(d, mealId, eaten) }
    }
}
