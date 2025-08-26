package com.food24.track.ui.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.food24.track.data.dao.MealDao
import com.food24.track.data.dao.MealEntryDao

class DayDetailsVMFactory(
    private val mealEntryDao: MealEntryDao,
    private val mealDao: MealDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DayDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DayDetailsViewModel(mealEntryDao, mealDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    companion object {
        fun from(app: android.app.Application): DayDetailsVMFactory {
            val db = com.food24.track.data.database.AppDatabase.getDatabase(app)
            return DayDetailsVMFactory(db.mealEntryDao(), db.mealDao())
        }
    }
}
