package com.food24.track.ui.meals

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food24.track.data.dao.DailyPlanDao
import com.food24.track.data.dao.MealDao
import com.food24.track.data.dao.MealEntryDao
import com.food24.track.data.dao.GoalDao
import com.food24.track.data.entity.DailyPlanEntity
import com.food24.track.data.entity.MealEntryEntity
import com.food24.track.data.entity.MealEntity
import com.food24.track.data.entity.MealTypes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class GenerateUiState(
    val goalType: GoalType = GoalType.LOSS,
    val activity: String = "Moderate",
    val weight: Float? = null,
    val height: Int? = null,
    val age: Int? = null,
    val mealsPerDay: Int = 5,
    val days: Int = 1
)

enum class GoalType(val server: String) { GAIN("Gain"), LOSS("Lose"), MAINTAIN("Maintain") }

class MealPlanViewModel(
    private val goalDao: GoalDao,
    private val dailyPlanDao: DailyPlanDao,
    private val mealEntryDao: MealEntryDao,
    private val mealDao: MealDao
) : ViewModel() {

    private val _state = MutableStateFlow(GenerateUiState())
    val state: StateFlow<GenerateUiState> = _state

    fun setGoalType(type: GoalType) { _state.value = _state.value.copy(goalType = type) }
    fun setMealsPerDay(count: Int) { _state.value = _state.value.copy(mealsPerDay = count) }
    fun setActivity(level: String) { _state.value = _state.value.copy(activity = level) }
    fun setAnthro(weight: Float?, height: Int?, age: Int?) {
        _state.value = _state.value.copy(weight = weight, height = height, age = age)
    }
    fun setDays(days: Int) { _state.value = _state.value.copy(days = days) }

    private data class Macro(val kcal: Int, val protein: Int, val fat: Int, val carbs: Int)

    private fun computeMacros(s: GenerateUiState): Macro {
        // Очень простой расчёт, просто чтобы были цифры
        // Подмените на свой PlanGenerator при желании
        val base = 28 * (s.weight ?: 75f) + 6 * (s.height ?: 175) - 5 * (s.age ?: 30) + 5
        val act = when (s.activity) {
            "Sedentary" -> 1.2f; "Light" -> 1.375f; "Moderate" -> 1.55f
            "Active" -> 1.725f; else -> 1.9f
        }
        var kcal = (base * act).toInt()
        kcal += when (s.goalType) {
            GoalType.GAIN -> 300
            GoalType.LOSS -> -300
            GoalType.MAINTAIN -> 0
        }
        val protein = (kcal * 0.3f / 4f).toInt()
        val fat     = (kcal * 0.25f / 9f).toInt()
        val carbs   = ((kcal - protein * 4 - fat * 9) / 4f).toInt()
        return Macro(kcal.coerceAtLeast(1200), protein, fat, carbs)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val ISO: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    @RequiresApi(Build.VERSION_CODES.O)
    fun generate(
        startDate: LocalDate = LocalDate.now(),
        onDone: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val s = state.value
                val days = s.days
                val mealsPerDay = s.mealsPerDay

                // гарантируем, что в таблице meals есть что выбрать
                ensureMealsSeed()

                repeat(days) { i ->
                    val dateStr = startDate.plusDays(i.toLong()).format(ISO)

                    // 1) DailyPlan
                    val macro = computeMacros(s)
                    dailyPlanDao.insert(
                        DailyPlanEntity(
                            date = dateStr,
                            totalCalories = macro.kcal,
                            protein = macro.protein,
                            fat = macro.fat,
                            carbs = macro.carbs
                        )
                    )

                    // 2) Подбираем блюда по типам и создаём MealEntry
                    val picked = pickMealsForDay(mealsPerDay)
                    val entries = picked.map { m ->
                        MealEntryEntity(
                            date = dateStr,
                            mealId = m.id,
                            eaten = false
                        )
                    }
                    mealEntryDao.insertAll(entries)
                }

                onDone()
            } catch (t: Throwable) {
                onError(t)
            }
        }
    }

    /** Берём 3-5 блюд по типам: breakfast, snack, lunch, dinner, post-workout */
    private suspend fun pickMealsForDay(count: Int): List<MealEntity> {
        val types = listOf(
            MealTypes.BREAKFAST,
            MealTypes.SNACK,
            MealTypes.LUNCH,
            MealTypes.DINNER,
            MealTypes.POST_WORKOUT
        )
        val poolByType = types.associateWith { mealDao.getByType(it) }
        val result = mutableListOf<MealEntity>()
        var idx = 0
        while (result.size < count) {
            val t = types[idx % types.size]
            val list = poolByType[t].orEmpty()
            if (list.isNotEmpty()) result += list[(result.size) % list.size]
            idx++
            if (idx > 20) break // защита от бесконечного цикла
        }
        return result
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
}
