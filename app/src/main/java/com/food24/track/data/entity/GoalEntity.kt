package com.food24.track.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Цели пользователя
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: Int = 1,
    val currentWeight: Float,
    val targetWeight: Float,
    val weeklyGoal: Float, // кг/нед
    val timeframeWeeks: Int,
    val dailyCalories: Int,
    val mealsPerDay: Int,
    val goalType: String // Lose / Gain / Maintain
)
