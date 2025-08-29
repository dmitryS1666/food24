package com.food24.track.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_log")
data class WeightLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,   // формат ISO yyyy-MM-dd
    val weightKg: Float
)
