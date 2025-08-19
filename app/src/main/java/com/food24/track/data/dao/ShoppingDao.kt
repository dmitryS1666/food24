package com.food24.track.data.dao

import androidx.room.*
import com.food24.track.data.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {

    @Query("SELECT * FROM shopping_items ORDER BY id DESC")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<ShoppingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingItemEntity)

    @Query("UPDATE shopping_items SET checked = :checked WHERE id = :id")
    suspend fun setChecked(id: Int, checked: Boolean)

    @Query("DELETE FROM shopping_items WHERE checked = 1")
    suspend fun clearChecked()

    @Query("DELETE FROM shopping_items")
    suspend fun clearAll()
}
