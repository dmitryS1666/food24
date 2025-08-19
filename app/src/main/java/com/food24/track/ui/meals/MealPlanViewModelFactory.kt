package com.food24.track.ui.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.food24.track.data.dao.GoalDao
import com.food24.track.data.database.AppDatabase
import com.food24.track.usecase.PlanGenerator

class MealPlanViewModelFactory(
    private val goalDao: GoalDao,
    private val generator: PlanGenerator
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealPlanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MealPlanViewModel(goalDao, generator) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    companion object {
        fun from(app: android.app.Application): MealPlanViewModelFactory {
            val db = AppDatabase.getDatabase(app)
            val generator = PlanGenerator(db.dailyPlanDao(), db.mealEntryDao(), db.mealDao())
            return MealPlanViewModelFactory(db.goalDao(), generator)
        }
    }
}
