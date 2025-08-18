package com.food24.track.usecase

import com.food24.track.data.dao.DailyPlanDao
import com.food24.track.data.dao.MealDao
import com.food24.track.data.dao.MealEntryDao
import com.food24.track.data.entity.DailyPlanEntity
import com.food24.track.data.entity.GoalEntity
import com.food24.track.data.entity.MealEntryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Простейший генератор плана питания.
 * Использует список готовых блюд (MealDao) и раскладывает их по дням
 * согласно целям пользователя (GoalEntity).
 */
class PlanGenerator(
    private val dailyPlanDao: DailyPlanDao,
    private val mealEntryDao: MealEntryDao,
    private val mealDao: MealDao
) {

    suspend fun generateForDate(date: String, goal: GoalEntity) = withContext(Dispatchers.IO) {
        val meals = mealDao.getAll() // предположим, у тебя есть DAO-метод getAll()

        if (meals.isEmpty()) return@withContext

        // очистим план на эту дату (если был)
        dailyPlanDao.deletePlanByDate(date)
        mealEntryDao.deleteByDate(date)

        // раскидываем блюда по количеству приёмов пищи
        val selected = meals.shuffled().take(goal.mealsPerDay)

        var totalCalories = 0
        selected.forEach { meal ->
            totalCalories += meal.calories
            mealEntryDao.insertEntry(
                MealEntryEntity(
                    date = date,
                    mealId = meal.id,
                    eaten = false
                )
            )
        }

        // сохраняем план
        dailyPlanDao.insertPlan(
            DailyPlanEntity(
                date = date,
                totalCalories = totalCalories,
                protein = 0, // пока нули, позже можно считать по блюдам
                fat = 0,
                carbs = 0
            )
        )
    }
}
