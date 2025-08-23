package com.food24.track.ui.shopping

object Categories {
    // Канонические названия (как в UI и в БД)
    val all = listOf(
        "Meat & Fish",
        "Vegetables & Fruits",
        "Grains & Carbs",
        "Oils & Condiments",
        "Dairy & Eggs",
        "Others"
    )

    // Быстрая нормализация ввода пользователя к канону (регистронечувствительно, лишние пробелы)
    fun canonicalOrOthers(input: String?): String {
        val inNorm = input?.trim()?.lowercase().orEmpty()
        val found = all.firstOrNull { it.lowercase() == inNorm }
        return found ?: "Others"
    }
}
