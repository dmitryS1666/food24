package com.food24.track.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.food24.track.data.dao.*

class ProgressVMFactory(
    private val weightDao: WeightLogDao,
    private val goalDao: GoalDao,
    private val dailyPlanDao: DailyPlanDao,
    private val mealEntryDao: MealEntryDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(cls: Class<T>): T {
        if (cls.isAssignableFrom(ProgressViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProgressViewModel(weightDao, goalDao, dailyPlanDao, mealEntryDao) as T
        }
        throw IllegalArgumentException("Unknown VM class")
    }
}
