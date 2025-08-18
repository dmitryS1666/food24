package com.food24.track.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.food24.track.data.dao.DailyPlanDao
import com.food24.track.data.dao.GoalDao
import com.food24.track.data.dao.MealDao
import com.food24.track.data.dao.MealEntryDao

class HomeDashboardViewModelFactory(
    private val goalDao: GoalDao,
    private val dailyPlanDao: DailyPlanDao,
    private val mealEntryDao: MealEntryDao,
    private val mealDao: MealDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeDashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeDashboardViewModel(
                goalDao, dailyPlanDao, mealEntryDao, mealDao
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
