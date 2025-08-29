package com.food24.track.ui.day

import com.food24.track.data.dao.MealDao
import com.food24.track.data.dao.MealEntryDao

class MealSectionVMFactory(
    private val mealEntryDao: MealEntryDao,
    private val mealDao: MealDao
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealSectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MealSectionViewModel(mealEntryDao, mealDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
