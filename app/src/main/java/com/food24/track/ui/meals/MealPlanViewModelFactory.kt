package com.food24.track.ui.meals

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.food24.track.data.dao.DailyPlanDao
import com.food24.track.data.dao.GoalDao
import com.food24.track.data.dao.MealDao
import com.food24.track.data.dao.MealEntryDao
import com.food24.track.data.database.AppDatabase

class MealPlanViewModelFactory(
    private val goalDao: GoalDao,
    private val dailyPlanDao: DailyPlanDao,
    private val mealEntryDao: MealEntryDao,
    private val mealDao: MealDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealPlanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MealPlanViewModel(
                goalDao = goalDao,
                dailyPlanDao = dailyPlanDao,
                mealEntryDao = mealEntryDao,
                mealDao = mealDao
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    companion object {
        fun from(app: Application): MealPlanViewModelFactory {
            val db = AppDatabase.getDatabase(app)
            return MealPlanViewModelFactory(
                goalDao = db.goalDao(),
                dailyPlanDao = db.dailyPlanDao(),
                mealEntryDao = db.mealEntryDao(),
                mealDao = db.mealDao()
            )
        }
    }
}
