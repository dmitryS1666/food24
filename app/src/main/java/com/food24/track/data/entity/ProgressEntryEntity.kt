package com.food24.track.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Запись прогресса (вес + калории)
@Entity(tableName = "progress_entries")
data class ProgressEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val weight: Float,
    val caloriesConsumed: Int
)
