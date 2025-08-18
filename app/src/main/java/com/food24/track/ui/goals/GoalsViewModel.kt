package com.food24.track.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food24.track.data.dao.GoalDao
import com.food24.track.data.entity.GoalEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GoalsViewModel(
    private val goalDao: GoalDao
) : ViewModel() {

    private val _goal = MutableStateFlow<GoalEntity?>(null)
    val goal: StateFlow<GoalEntity?> = _goal

    fun loadGoal() {
        viewModelScope.launch {
            _goal.value = goalDao.getGoal()
        }
    }

    fun saveGoal(goal: GoalEntity) {
        viewModelScope.launch {
            goalDao.saveGoal(goal)
            _goal.value = goal
        }
    }
}
