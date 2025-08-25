package com.food24.track.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.food24.track.data.entity.MealEntity

@Dao
interface MealDao {
    @Query("SELECT * FROM meals WHERE type = :type")
    suspend fun getMealsByType(type: String): List<MealEntity>

    @Query("SELECT * FROM meals WHERE id = :id")
    suspend fun getById(id: Int): MealEntity

    @Query("SELECT * FROM meals")
    suspend fun getAll(): List<MealEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeals(meals: List<MealEntity>)

    @Query("SELECT COUNT(*) FROM meals") suspend fun countAll(): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(list: List<MealEntity>)
    @Query("SELECT * FROM meals WHERE type = :type") suspend fun getByType(type: String): List<MealEntity>
    @Query("SELECT * FROM meals WHERE id IN (:ids)") suspend fun getByIds(ids: List<Int>): List<MealEntity>
}
