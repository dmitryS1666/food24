package com.food24.track.ui.meals

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food24.track.data.dao.GoalDao
import com.food24.track.data.entity.GoalEntity
import com.food24.track.usecase.PlanGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class MealPlanViewModel(
    private val goalDao: GoalDao,
    private val planGenerator: PlanGenerator
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun generate(startDate: LocalDate = LocalDate.now(), onDone: () -> Unit, onError: (Throwable)->Unit) {
        viewModelScope.launch {
            try {
                // Берём текущую цель как основу и применяем значения из UI (mealsPerDay и тип цели)
                val base = goalDao.getGoal() ?: GoalEntity(
                    id = 1,
                    currentWeight = _state.value.weight ?: 70f,
                    targetWeight = (_state.value.weight ?: 70f) + when (_state.value.goalType) {
                        GoalType.GAIN -> 2f
                        GoalType.LOSS -> -2f
                        else -> 0f
                    },
                    weeklyGoal = if (_state.value.goalType == GoalType.LOSS) -0.5f else 0.3f,
                    timeframeWeeks = 4,
                    dailyCalories = 2000,
                    mealsPerDay = _state.value.mealsPerDay,
                    goalType = _state.value.goalType.server
                )

                val goal = base.copy(
                    mealsPerDay = _state.value.mealsPerDay,
                    goalType = _state.value.goalType.server
                )

                repeat(_state.value.days) { i ->
                    val date = startDate.plusDays(i.toLong()).toString()
                    planGenerator.generateForDate(date, goal)
                }
                onDone()
            } catch (t: Throwable) {
                onError(t)
            }
        }
    }
}

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
