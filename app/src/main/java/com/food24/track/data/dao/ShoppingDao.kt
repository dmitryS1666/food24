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

    @Query("UPDATE shopping_items SET checked = 0 WHERE checked = 1")
    suspend fun clearChecked()

    @Query("SELECT COUNT(*) FROM shopping_items")
    suspend fun countItems(): Int

    @Query("DELETE FROM shopping_items")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM shopping_items WHERE name = :name")
    suspend fun countByName(name: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(list: List<ShoppingItemEntity>)

    @Query("SELECT * FROM shopping_items")
    suspend fun getAllOnce(): List<ShoppingItemEntity>

    @Query("SELECT category, COUNT(*) as cnt FROM shopping_items GROUP BY category")
    suspend fun countsByCategory(): List<CategoryCount>

    data class CategoryCount(val category: String, val cnt: Int)
}
