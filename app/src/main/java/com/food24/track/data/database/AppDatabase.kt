package com.food24.track.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.food24.track.data.dao.DailyPlanDao
import com.food24.track.data.dao.GoalDao
import com.food24.track.data.dao.MealDao
import com.food24.track.data.dao.MealEntryDao
import com.food24.track.data.dao.ProgressDao
import com.food24.track.data.dao.ShoppingDao
import com.food24.track.data.entity.DailyPlanEntity
import com.food24.track.data.entity.GoalEntity
import com.food24.track.data.entity.MealEntity
import com.food24.track.data.entity.MealEntryEntity
import com.food24.track.data.entity.ProgressEntryEntity
import com.food24.track.data.entity.ShoppingItemEntity

@Database(
    entities = [
        MealEntity::class,
        DailyPlanEntity::class,
        MealEntryEntity::class,
        ShoppingItemEntity::class,
        ProgressEntryEntity::class,
        GoalEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun dailyPlanDao(): DailyPlanDao
    abstract fun mealEntryDao(): MealEntryDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun progressDao(): ProgressDao
    abstract fun goalDao(): GoalDao
}
