package com.food24.track.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.food24.track.data.entity.GoalEntity

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals LIMIT 1")
    suspend fun getGoal(): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGoal(goal: GoalEntity)

    @Query("SELECT * FROM goals LIMIT 1")
    fun observeGoal(): kotlinx.coroutines.flow.Flow<GoalEntity?>
}
