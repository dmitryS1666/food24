package com.food24.track.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Элемент списка покупок
@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: String,
    val category: String,
    val checked: Boolean = false
)
