package com.food24.track.ui.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food24.track.data.dao.MealDao
import com.food24.track.data.dao.MealEntryDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ui/day/MealSectionViewModel.kt
class MealSectionViewModel(
    private val entryDao: MealEntryDao,
    private val mealDao: MealDao
) : ViewModel() {

    private val _items = MutableStateFlow<List<MealItemUi>>(emptyList())
    val items: StateFlow<List<MealItemUi>> = _items

    private var date = ""
    private var type = ""

    fun bind(dateIso: String, type: String) {
        date = dateIso; this.type = type
        viewModelScope.launch {
            entryDao.observeEntriesByDate(dateIso).collect { entries ->
                val ids = entries.map { it.mealId }.distinct()
                val meals = if (ids.isEmpty()) emptyList() else mealDao.getByIds(ids)
                val byId = meals.associateBy { it.id }

                _items.value = entries.mapNotNull { e ->
                    val m = byId[e.mealId] ?: return@mapNotNull null
                    if (m.type != type) return@mapNotNull null
                    MealItemUi(e.mealId, m.name, m.calories, e.eaten)
                }
            }
        }
    }

    fun toggle(mealId: Int, eaten: Boolean) {
        viewModelScope.launch { entryDao.setEaten(date, mealId, eaten) }
    }
}

data class MealItemUi(val id: Int, val title: String, val kcal: Int, val eaten: Boolean)
