package com.food24.track.data.dao

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.food24.track.data.entity.MealEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

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

    // data/dao/MealEntryDao.kt
    data class DailyConsumed(
        val date: String,        // ISO yyyy-MM-dd
        val consumedKcal: Int
    )

    @Query(
        """
            SELECT me.date AS date, 
                   SUM(CASE WHEN me.eaten = 1 THEN m.calories ELSE 0 END) AS consumedKcal
            FROM meal_entries me
            JOIN meals m ON m.id = me.mealId
            WHERE me.date >= date(:fromIso)
            GROUP BY me.date
            ORDER BY me.date ASC
        """
    )
    fun observeFromDate(fromIso: String): Flow<List<DailyConsumed>>

    @RequiresApi(Build.VERSION_CODES.O)
    fun observeLastNDays(n: Int): Flow<List<DailyConsumed>> =
        observeFromDate(LocalDate.now().minusDays(n.toLong()).toString())

}
