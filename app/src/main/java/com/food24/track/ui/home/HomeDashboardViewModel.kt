package com.food24.track.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food24.track.data.dao.DailyPlanDao
import com.food24.track.data.dao.GoalDao
import com.food24.track.data.dao.MealDao
import com.food24.track.data.dao.MealEntryDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class HomeDashboardViewModel(
    private val goalDao: GoalDao,
    private val dailyPlanDao: DailyPlanDao,
    private val mealEntryDao: MealEntryDao,
    private val mealDao: MealDao
) : ViewModel() {

    private val _ui = MutableStateFlow(HomeUiState())
    val ui: StateFlow<HomeUiState> = _ui

    fun load(date: String) {
        viewModelScope.launch {
            val goal = goalDao.getGoal()
            val plan = dailyPlanDao.getPlanByDate(date)

            // 1) берём один срез из Flow<List<MealEntryEntity>>
            val entries = mealEntryDao.getEntriesByDate(date).first()

            // 2) считаем прогресс и собираем карточки через обычный цикл,
            //    т.к. внутри нужна suspend-функция mealDao.getById(...)
            var cals = 0; var p = 0; var f = 0; var cb = 0
            val cards = mutableListOf<MealCardUi>()

            for (e in entries) {
                val m = mealDao.getById(e.mealId) ?: continue
                if (e.eaten) {
                    cals += m.calories; p += m.protein; f += m.fat; cb += m.carbs
                }
                cards += MealCardUi(m.id, m.name, m.calories, e.eaten, m.type)
            }

            val targetKcal = goal?.dailyCalories ?: (plan?.totalCalories ?: 0)

            _ui.value = HomeUiState(
                date = date,
                goalTitle = when (goal?.goalType) { "Lose" -> "Lose Fat"; "Gain" -> "Weight Gain"; else -> "Maintain" },
                targetRange = if (goal != null) "${(goal.dailyCalories - 200).coerceAtLeast(0)}–${goal.dailyCalories + 200} kcal" else "",
                consumed = cals, target = targetKcal,
                protein = p to (plan?.protein ?: 0),
                fat = f to (plan?.fat ?: 0),
                carbs = cb to (plan?.carbs ?: 0),
                mealCards = cards
            )
        }
    }
}

data class HomeUiState(
    val date: String = "",
    val goalTitle: String = "",
    val targetRange: String = "",
    val consumed: Int = 0,
    val target: Int = 0,
    val protein: Pair<Int, Int> = 0 to 0, // consumed / target
    val fat: Pair<Int, Int> = 0 to 0,
    val carbs: Pair<Int, Int> = 0 to 0,
    val mealCards: List<MealCardUi> = emptyList()
)

data class MealCardUi(
    val id: Int,
    val title: String,
    val calories: Int,
    val eaten: Boolean,
    val type: String
)
