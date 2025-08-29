package com.food24.track.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.food24.track.data.entity.WeightLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WeightLogEntity)

    @Query("DELETE FROM weight_log")
    suspend fun clear()

    @Query("SELECT * FROM weight_log ORDER BY date ASC")
    fun observeAll(): Flow<List<WeightLogEntity>>
}
