package com.food24.track.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.food24.track.data.entity.DailyPlanEntity

@Dao
interface DailyPlanDao {
    @Query("SELECT * FROM daily_plans WHERE date = :date")
    suspend fun getPlanByDate(date: String): DailyPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: DailyPlanEntity)

    @Query("DELETE FROM daily_plans WHERE date = :date")
    suspend fun deletePlanByDate(date: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: DailyPlanEntity)

    @Query("SELECT * FROM daily_plans WHERE date = :date LIMIT 1")
    fun observePlanByDate(date: String): kotlinx.coroutines.flow.Flow<DailyPlanEntity?>
}
