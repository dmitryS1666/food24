package com.food24.track.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.food24.track.data.entity.ProgressEntryEntity

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress_entries ORDER BY date")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<ProgressEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ProgressEntryEntity)

    @Query("DELETE FROM progress_entries")
    suspend fun clearAll()
}
