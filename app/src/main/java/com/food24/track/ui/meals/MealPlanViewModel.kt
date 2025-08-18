package com.food24.track.ui.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food24.track.data.dao.GoalDao
import com.food24.track.usecase.PlanGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MealPlanViewModel(
    private val goalDao: GoalDao,
    private val generator: PlanGenerator
) : ViewModel() {

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status

    fun generatePlan(date: String) {
        viewModelScope.launch {
            val goal = goalDao.getGoal()
            if (goal != null) {
                generator.generateForDate(date, goal)
                _status.value = "Plan generated for $date"
            }
        }
    }
}
