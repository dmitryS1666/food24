package com.food24.track.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.food24.track.data.entity.MealEntryEntity

@Dao
interface MealEntryDao {
    @Query("SELECT * FROM meal_entries WHERE date = :date")
    suspend fun getEntriesByDate(date: String): List<MealEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<MealEntryEntity>)

    @Update
    suspend fun updateEntry(entry: MealEntryEntity)
}
