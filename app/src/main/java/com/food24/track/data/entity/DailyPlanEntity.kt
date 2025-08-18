package com.food24.track.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// План питания на день
@Entity(tableName = "daily_plans")
data class DailyPlanEntity(
    @PrimaryKey val date: String, // формат yyyy-MM-dd
    val totalCalories: Int,
    val protein: Int,
    val fat: Int,
    val carbs: Int
)
