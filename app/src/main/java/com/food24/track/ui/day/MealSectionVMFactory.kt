package com.food24.track.ui.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.food24.track.data.dao.MealDao
import com.food24.track.data.dao.MealEntryDao

class MealSectionVMFactory(
    private val entryDao: MealEntryDao,
    private val mealDao: MealDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealSectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MealSectionViewModel(entryDao, mealDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}