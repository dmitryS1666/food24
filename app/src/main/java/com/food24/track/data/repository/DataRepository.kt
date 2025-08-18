package com.food24.track.data.repository

import com.food24.track.data.dao.DailyPlanDao
import com.food24.track.data.dao.GoalDao
import com.food24.track.data.dao.MealDao
import com.food24.track.data.dao.MealEntryDao
import com.food24.track.data.dao.ProgressDao
import com.food24.track.data.dao.ShoppingDao

class DataRepository(
    private val dailyPlanDao: DailyPlanDao,
    private val goalDao: GoalDao,
    private val mealDao: MealDao,
    private val mealEntryDao: MealEntryDao,
    private val progressDao: ProgressDao,
    private val shoppingDao: ShoppingDao
) {
    suspend fun clearAllData() {
//        dailyPlanDao.clearAll()
//        goalDao.clearAll()
//        mealDao.clearAll()
//        mealEntryDao.clearAll()
//        progressDao.clearAll()
//        shoppingDao.clearAll()
    }
}