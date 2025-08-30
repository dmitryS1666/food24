package com.food24.track.ui.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food24.track.data.dao.MealDao
import com.food24.track.data.dao.MealEntryDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
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

    private val _items = kotlinx.coroutines.flow.MutableStateFlow<List<MealItemUi>>(emptyList())
    val items: kotlinx.coroutines.flow.StateFlow<List<MealItemUi>> = _items

    fun bind(dateIso: String, type: String) {
        viewModelScope.launch {
            mealEntryDao.observeEntriesByDate(dateIso)                // Flow<List<MealEntryEntity>>
                .flatMapLatest { entries ->
                    val ids = entries.map { it.mealId }.distinct()
                    kotlinx.coroutines.flow.flow {
                        // разовая выборка блюд по id
                        val meals = if (ids.isEmpty()) emptyList() else mealDao.getByIds(ids)
                        val byId = meals.associateBy { it.id }
                        val filtered = entries.mapNotNull { e ->
                            val m = byId[e.mealId] ?: return@mapNotNull null
                            if (m.type != type) return@mapNotNull null
                            MealItemUi(
                                entryId = m.id,
                                mealId = m.id,
                                title = m.name,
                                calories = m.calories,
                                eaten = e.eaten
                            )
                        }
                        emit(filtered)
                    }
                }
                .collect { _items.value = it }
        }
    }

    fun toggle(entryId: Int, eaten: Boolean) {
        viewModelScope.launch {
            mealEntryDao.setEaten(entryId, eaten)
        }
    }
}

data class MealItemUi(
    val entryId: Int,
    val mealId: Int,
    val title: String,
    val calories: Int,
    val eaten: Boolean
)
