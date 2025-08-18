package com.food24.track.data.entity

import androidx.room.Entity

// Конкретное блюдо в плане
@Entity(
    tableName = "meal_entries",
    primaryKeys = ["date", "mealId"]
)
data class MealEntryEntity(
    val date: String,
    val mealId: Int,
    val eaten: Boolean = false
)
