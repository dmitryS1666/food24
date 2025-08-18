package com.food24.track.ui.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food24.track.data.dao.MealDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MealDetailViewModel(
    private val mealDao: MealDao
) : ViewModel() {

    private val _meal = MutableStateFlow<UiMealDetail?>(null)
    val meal: StateFlow<UiMealDetail?> = _meal

    fun loadMeal(mealId: Int) {
        viewModelScope.launch {
            mealDao.getById(mealId)?.let {
                _meal.value = UiMealDetail(it.id, it.name, it.calories, it.protein, it.fat, it.carbs)
            }
        }
    }
}

data class UiMealDetail(
    val id: Int,
    val name: String,
    val calories: Int,
    val protein: Int,
    val fat: Int,
    val carbs: Int
)
