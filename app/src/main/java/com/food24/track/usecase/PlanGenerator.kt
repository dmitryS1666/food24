package com.food24.track.usecase

import com.food24.track.data.dao.*
import com.food24.track.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

class PlanGenerator(
    private val dailyPlanDao: DailyPlanDao,
    private val mealEntryDao: MealEntryDao,
    private val mealDao: MealDao
) {
    // простая разбивка калорий на 5 приёмов как в макете (20/10/30/10/30)
    private fun split(target: Int, mealsPerDay: Int): List<Int> =
        if (mealsPerDay == 5)
            listOf(0.20f, 0.10f, 0.30f, 0.10f, 0.30f).map { (target * it).toInt() }
        else List(mealsPerDay) { target / mealsPerDay }

    private fun pickClosest(pool: List<MealEntity>, target: Int): MealEntity =
        pool.minByOrNull { abs(it.calories - target) } ?: pool.random()

    suspend fun generateForDate(date: String, goal: GoalEntity) = withContext(Dispatchers.IO) {
        // очистим прежний план на эту дату
        dailyPlanDao.deletePlanByDate(date)
        mealEntryDao.deleteByDate(date)

        val mealTypes = listOf("Breakfast", "Snack", "Lunch", "Snack", "Dinner", "PostWorkout")
            .take(goal.mealsPerDay)
        val kcalPerMeal = split(goal.dailyCalories, goal.mealsPerDay)

        var totalC = 0; var P = 0; var F = 0; var C = 0
        val entries = mutableListOf<MealEntryEntity>()

        mealTypes.forEachIndexed { i, type ->
            val pool = mealDao.getMealsByType(type).ifEmpty { mealDao.getAll() }
            if (pool.isNotEmpty()) {
                val m = pickClosest(pool, kcalPerMeal[i])
                totalC += m.calories; P += m.protein; F += m.fat; C += m.carbs
                entries += MealEntryEntity(date = date, mealId = m.id, eaten = false)
            }
        }

        dailyPlanDao.insertPlan(
            DailyPlanEntity(
                date = date,
                totalCalories = totalC,
                protein = P, fat = F, carbs = C
            )
        )
        mealEntryDao.insertEntries(entries)
    }
}
