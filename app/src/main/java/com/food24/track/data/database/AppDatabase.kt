package com.food24.track.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.food24.track.data.dao.*
import com.food24.track.data.entity.*

@Database(
    entities = [
        MealEntity::class,
        DailyPlanEntity::class,
        MealEntryEntity::class,
        ShoppingItemEntity::class,
        ProgressEntryEntity::class,
        GoalEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun mealDao(): MealDao
    abstract fun dailyPlanDao(): DailyPlanDao
    abstract fun mealEntryDao(): MealEntryDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun progressDao(): ProgressDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "food24_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
