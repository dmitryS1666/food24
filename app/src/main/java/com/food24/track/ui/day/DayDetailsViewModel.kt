package com.food24.track.ui.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food24.track.data.dao.MealDao
import com.food24.track.data.dao.MealEntryDao
import com.food24.track.data.entity.MealTypes
import kotlinx.coroutines.launch
import com.food24.track.R
import com.food24.track.data.entity.MealEntity
import com.food24.track.data.entity.MealEntryEntity

class DayDetailsViewModel(
    private val mealEntryDao: MealEntryDao,
    private val mealDao: MealDao
) : ViewModel() {

    private val _sections = kotlinx.coroutines.flow.MutableStateFlow<List<DaySectionUi>>(emptyList())
    val sections: kotlinx.coroutines.flow.StateFlow<List<DaySectionUi>> = _sections

    fun bind(dateIso: String) {
        viewModelScope.launch {
            mealEntryDao.observeEntriesByDate(dateIso).collect { entries ->
                val ids = entries.map { it.mealId }.distinct()
                val meals = if (ids.isEmpty()) emptyList() else mealDao.getByIds(ids)
                val byId = meals.associateBy { it.id }

                val grouped = entries.groupBy { byId[it.mealId]?.type.orEmpty() }

                val list = grouped.map { (type, ents) ->
                    val totalKcal = ents.sumOf { byId[it.mealId]?.calories ?: 0 }
                    val eatenCount = ents.count { it.eaten }
                    val itemsCount = ents.size
                    DaySectionUi(
                        type = type,
                        title = when (type) {
                            MealTypes.BREAKFAST    -> "Breakfast"
                            MealTypes.SNACK        -> "Snack"
                            MealTypes.LUNCH        -> "Lunch"
                            MealTypes.DINNER       -> "Dinner"
                            MealTypes.POST_WORKOUT -> "Post-Workout"
                            else                   -> type.ifBlank { "Other" }
                        },
                        totalKcal = totalKcal,
                        itemsCount = itemsCount,
                        eatenCount = eatenCount,
                        thumbRes = when (type) {
                            MealTypes.BREAKFAST    -> R.drawable.meal_breakfast
                            MealTypes.SNACK        -> R.drawable.meal_snack
                            MealTypes.LUNCH        -> R.drawable.meal_lunch
                            MealTypes.DINNER       -> R.drawable.meal_dinner
                            MealTypes.POST_WORKOUT -> R.drawable.meal_postworkout
                            else                   -> R.drawable.placeholder_meal
                        }
                    )
                }.sortedBy { orderOfType(it.type) }

                _sections.value = list
            }
        }
    }

    fun regenerateDay(
        dateIso: String,
        mealsPerDay: Int = 4,                        // минимально: 4 (завтрак, снэк, обед, ужин)
        onDone: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 1) очистить записи
                mealEntryDao.deleteByDate(dateIso)

                // 2) убедиться, что есть из чего генерить
                ensureMealsSeed()

                // 3) подобрать N блюд (по кругу по типам)
                val picked = pickMealsForDay(mealsPerDay)

                // 4) вставить новые записи
                val entries = picked.map { m ->
                    MealEntryEntity(date = dateIso, mealId = m.id, eaten = false)
                }
                mealEntryDao.insertAll(entries)

                onDone()
                // обновим экран
                // (если у тебя bind(dateIso) подписан на flow, он сам обновится)
            } catch (t: Throwable) { onError(t) }
        }
    }

    private suspend fun ensureMealsSeed() {
        if (mealDao.countAll() > 0) return
        val seed = listOf(
            MealEntity(
                name = "Oatmeal with berries",
                calories = 320, protein = 12, fat = 7, carbs = 55,
                type = MealTypes.BREAKFAST,
                ingredients = "Oats 50g, Milk 200ml, Blueberries 50g, Honey 1 tsp"
            ),
            MealEntity(
                name = "Greek yogurt",
                calories = 180, protein = 16, fat = 5, carbs = 18,
                type = MealTypes.SNACK,
                ingredients = "Greek yogurt 150g, Honey 1 tsp, Walnuts 10g"
            ),
            MealEntity(
                name = "Chicken & rice",
                calories = 520, protein = 35, fat = 10, carbs = 70,
                type = MealTypes.LUNCH,
                ingredients = "Chicken breast 150g, White rice 100g, Olive oil 1 tsp, Broccoli 80g"
            ),
            MealEntity(
                name = "Salmon with veggies",
                calories = 480, protein = 34, fat = 20, carbs = 28,
                type = MealTypes.DINNER,
                ingredients = "Salmon fillet 150g, Zucchini 100g, Carrots 80g, Olive oil 1 tsp"
            ),
            MealEntity(
                name = "Protein shake",
                calories = 220, protein = 30, fat = 4, carbs = 16,
                type = MealTypes.POST_WORKOUT,
                ingredients = "Whey protein 1 scoop, Milk 200ml, Banana 100g"
            )
        )
        mealDao.insertAll(seed)
    }

    private suspend fun pickMealsForDay(count: Int): List<MealEntity> {
        val types = listOf(
            MealTypes.BREAKFAST, MealTypes.SNACK, MealTypes.LUNCH, MealTypes.DINNER, MealTypes.POST_WORKOUT
        )
        val pools = types.associateWith { mealDao.getByType(it) }
        val out = mutableListOf<MealEntity>()
        var i = 0
        while (out.size < count) {
            val t = types[i % types.size]
            val list = pools[t].orEmpty()
            if (list.isNotEmpty()) out += list[out.size % list.size]
            i++
            if (i > 20) break
        }
        return out
    }

    private fun orderOfType(type: String) = when (type) {
        MealTypes.BREAKFAST -> 0
        MealTypes.SNACK -> 1
        MealTypes.LUNCH -> 2
        MealTypes.DINNER -> 3
        MealTypes.POST_WORKOUT -> 4
        else -> 99
    }
}
