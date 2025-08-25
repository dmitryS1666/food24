package com.food24.track.ui.home

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.food24.track.data.dao.DailyPlanDao
import com.food24.track.data.dao.GoalDao
import com.food24.track.data.dao.MealDao
import com.food24.track.data.dao.MealEntryDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeDashboardViewModel(
    private val goalDao: GoalDao,
    private val dailyPlanDao: DailyPlanDao,
    private val mealEntryDao: MealEntryDao,
    private val mealDao: MealDao
) : ViewModel() {

    private val _ui = MutableStateFlow(HomeUiState())
    val ui: StateFlow<HomeUiState> = _ui

    // храним выбранную дату (ISO yyyy-MM-dd)
    private val dateFlow = MutableStateFlow("")

    @OptIn(UnstableApi::class)
    fun bind(dateIso: String) {
        // если дата не менялась — выходим
        if (dateFlow.value == dateIso) return
        dateFlow.value = dateIso

        viewModelScope.launch {
            // на каждую дату пересобираем подписку
            dateFlow.collect { date ->
                // отменяем предыдущий сбор (launchIn/collectLatest)
                combine(
                    goalDao.observeGoal(),
                    dailyPlanDao.observePlanByDate(date),
                    mealEntryDao.observeEntriesByDate(date)
                ) { goal, plan, entries ->
                    // подтянем все блюда разом
                    val mealIds = entries.map { it.mealId }.distinct()
                    val meals = if (mealIds.isEmpty()) emptyList() else mealDao.getByIds(mealIds)
                    val mealById = meals.associateBy { it.id }

                    var cals = 0; var p = 0; var f = 0; var cb = 0
                    val cards = mutableListOf<MealCardUi>()

                    Log.d("HOME", "date=${date}, entries=${entries.size}, plan=${plan?.date}, goal=${goal?.goalType}")
                    entries.take(3).forEach { e ->
                        android.util.Log.d("HOME", "mealId=${e.mealId} date=${e.date} eaten=${e.eaten}")
                    }

                    entries.forEach { e ->
                        val m = mealById[e.mealId] ?: return@forEach
                        if (e.eaten) {
                            cals += m.calories; p += m.protein; f += m.fat; cb += m.carbs
                        }
                        cards += MealCardUi(m.id, m.name, m.calories, e.eaten, m.type)
                    }

                    val targetKcal = goal?.dailyCalories ?: (plan?.totalCalories ?: 0)
                    HomeUiState(
                        date = date,
                        goalTitle = when (goal?.goalType) {
                            "Lose" -> "Lose Fat"
                            "Gain" -> "Weight Gain"
                            else   -> "Maintain"
                        },
                        targetRange = goal?.let {
                            "${(it.dailyCalories - 200).coerceAtLeast(0)}–${it.dailyCalories + 200} kcal"
                        } ?: "",
                        consumed = cals, target = targetKcal,
                        protein = p to (plan?.protein ?: 0),
                        fat     = f to (plan?.fat ?: 0),
                        carbs   = cb to (plan?.carbs ?: 0),
                        mealCards = cards
                    )
                }.collect { state -> _ui.value = state }
            }
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
