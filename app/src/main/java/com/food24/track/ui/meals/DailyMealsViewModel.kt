package com.food24.track.ui.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food24.track.data.dao.MealDao
import com.food24.track.data.dao.MealEntryDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// DailyMealsViewModel.kt
class DailyMealsViewModel(
    private val mealDao: MealDao,
    private val mealEntryDao: MealEntryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyMealsUiState())
    val uiState: StateFlow<DailyMealsUiState> = _uiState

    fun loadMeals(date: String) {
        viewModelScope.launch {
            mealEntryDao.getEntriesByDate(date).collect { entries ->
                val meals = entries.mapNotNull { entry ->
                    mealDao.getById(entry.mealId)?.let { m ->
                        UiMeal(
                            entryId = entry.mealId,     // <-- берем id записи из entries
                            mealId  = m.id,
                            name    = m.name,
                            calories= m.calories,
                            eaten   = entry.eaten
                        )
                    }
                }
                _uiState.value = DailyMealsUiState(meals)
            }
        }
    }

    fun markEaten(date: String, mealId: Int) {
        viewModelScope.launch {
            mealEntryDao.setEaten(date, mealId, true)
            loadMeals(date)
        }
    }
}

data class DailyMealsUiState(val meals: List<UiMeal> = emptyList())

data class UiMeal(
    val entryId: Int,   // ВАЖНО: это id строки из meal_entries
    val mealId: Int,
    val name: String,
    val calories: Int,
    val eaten: Boolean
)
