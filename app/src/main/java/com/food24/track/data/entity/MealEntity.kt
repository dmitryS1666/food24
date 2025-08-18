package com.food24.track.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Блюдо из базы рецептов
@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val calories: Int,
    val protein: Int,
    val fat: Int,
    val carbs: Int,
    val type: String, // Breakfast / Snack / Lunch / Dinner / PostWorkout
    val ingredients: String // JSON-строка или CSV
)
