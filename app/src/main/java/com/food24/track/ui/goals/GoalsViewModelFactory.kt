package com.food24.track.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.food24.track.data.dao.GoalDao

class GoalsViewModelFactory(private val goalDao: GoalDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GoalsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GoalsViewModel(goalDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
