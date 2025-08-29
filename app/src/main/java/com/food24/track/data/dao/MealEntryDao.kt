package com.food24.track.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.food24.track.data.entity.MealEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: MealEntryEntity)

    @Query("SELECT * FROM meal_entries WHERE date = :date")
    fun getEntriesByDate(date: String): Flow<List<MealEntryEntity>>

    @Query("DELETE FROM meal_entries WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("UPDATE meal_entries SET eaten = :eaten WHERE date = :date AND mealId = :mealId")
    suspend fun setEaten(date: String, mealId: Int, eaten: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(list: List<MealEntryEntity>)

    @Query("DELETE FROM meal_entries WHERE date = :date AND mealId = :mealId")
    suspend fun deleteEntry(date: String, mealId: Int)

    @Query("SELECT * FROM meal_entries WHERE date = :date")
    fun observeByDate(date: String): Flow<List<MealEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MealEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<MealEntryEntity>)

    @Query("SELECT * FROM meal_entries WHERE date = :date")
    fun observeEntriesByDate(date: String): kotlinx.coroutines.flow.Flow<List<MealEntryEntity>>

    @Query("UPDATE meal_entries SET eaten = :eaten WHERE date = :date AND mealId = :mealId")
    suspend fun updateEaten(date: String, mealId: Int, eaten: Boolean)
}
