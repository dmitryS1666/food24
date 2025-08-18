package com.food24.track.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food24.track.data.dao.DailyPlanDao
import com.food24.track.data.dao.GoalDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeDashboardViewModel(
    private val dailyPlanDao: DailyPlanDao,
    private val goalDao: GoalDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    fun loadDashboard(date: String) {
        viewModelScope.launch {
            val goal = goalDao.getGoal()
            val plan = dailyPlanDao.getPlanByDate(date)
            _uiState.value = HomeUiState(
                targetCalories = goal?.dailyCalories ?: 0,
                currentCalories = plan?.totalCalories ?: 0
            )
        }
    }
}

data class HomeUiState(
    val currentCalories: Int = 0,
    val targetCalories: Int = 0
)
