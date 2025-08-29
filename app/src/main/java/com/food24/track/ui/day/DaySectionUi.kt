package com.food24.track.ui.day

data class DaySectionUi(
    val type: String,       // MealTypes.BREAKFAST / MealTypes.SNACK / ...
    val title: String,      // "Breakfast"
    val totalKcal: Int,     // суммарные калории по блюдам секции
    val itemsCount: Int,    // всего блюд в секции
    val eatenCount: Int,    // сколько блюд съедено
    val thumbRes: Int       // ресурс иконки секции (drawable)
)
