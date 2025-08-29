package com.food24.track.ui.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food24.track.data.dao.MealDao
import com.food24.track.data.dao.MealEntryDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class MealRowUi(
    val mealId: Int,
    val title: String,
    val calories: Int,
    val eaten: Boolean
)

class MealSectionViewModel(
    private val mealEntryDao: MealEntryDao,
    private val mealDao: MealDao
) : ViewModel() {

    private val _items = MutableStateFlow<List<MealRowUi>>(emptyList())
    val items: StateFlow<List<MealRowUi>> = _items

    private var currentDate = ""
    private var currentType = ""

    fun bind(dateIso: String, type: String) {
        currentDate = dateIso
        currentType = type

        viewModelScope.launch {
            mealEntryDao.observeEntriesByDate(dateIso).collectLatest { entries ->
                // нужны только записи этого типа -> узнаём тип через MealEntity
                val ids = entries.map { it.mealId }.distinct()
                val meals = if (ids.isEmpty()) emptyList() else mealDao.getByIds(ids)
                val byId = meals.associateBy { it.id }

                val filtered = entries.mapNotNull { e ->
                    val m = byId[e.mealId] ?: return@mapNotNull null
                    if (m.type != type) return@mapNotNull null
                    MealRowUi(
                        mealId = m.id,
                        title = m.name,
                        calories = m.calories,
                        eaten = e.eaten
                    )
                }
                _items.value = filtered
            }
        }
    }

    fun toggle(mealId: Int, eaten: Boolean) {
        val date = currentDate
        if (date.isBlank()) return
        viewModelScope.launch {
            // см. п.3 — нужен DAO метод updateEaten(...)
            mealEntryDao.updateEaten(date, mealId, eaten)
        }
    }
}
