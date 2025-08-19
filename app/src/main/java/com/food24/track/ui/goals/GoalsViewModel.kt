package com.food24.track.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food24.track.data.dao.GoalDao
import com.food24.track.data.entity.GoalEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class GoalType(val server: String) { GAIN("Gain"), LOSS("Lose"), MAINTAIN("Maintain") }

data class GoalsUiState(
    val weight: Float? = null,
    val targetWeight: Float? = null,
    val weeklyGoalKg: Float? = null,  // кг/неделю, может быть отрицательным
    val timeframeWeeks: Int? = null,
    val goalType: GoalType = GoalType.LOSS,
    val mealsPerDay: Int = 5,
    val maintenance: Int = 0,
    val dailyTarget: Int = 0
)

class GoalsViewModel(
    private val goalDao: GoalDao
) : ViewModel() {

    private val _ui = MutableStateFlow(GoalsUiState())
    val ui: StateFlow<GoalsUiState> = _ui

    fun load() {
        viewModelScope.launch {
            val g = goalDao.getGoal()
            if (g != null) {
                _ui.value = _ui.value.copy(
                    weight = g.currentWeight,
                    targetWeight = g.targetWeight,
                    weeklyGoalKg = g.weeklyGoal,
                    timeframeWeeks = g.timeframeWeeks,
                    goalType = when (g.goalType) {
                        "Gain" -> GoalType.GAIN
                        "Maintain" -> GoalType.MAINTAIN
                        else -> GoalType.LOSS
                    },
                    mealsPerDay = g.mealsPerDay
                ).recalc()
            } else {
                _ui.value = _ui.value.recalc()
            }
        }
    }

    fun setWeight(v: Float?) { _ui.value = _ui.value.copy(weight = v).recalc() }
    fun setTargetWeight(v: Float?) { _ui.value = _ui.value.copy(targetWeight = v).recalc() }
    fun setWeeklyGoal(v: Float?) { _ui.value = _ui.value.copy(weeklyGoalKg = v).recalc() }
    fun setTimeframe(v: Int?) { _ui.value = _ui.value.copy(timeframeWeeks = v) }
    fun setGoalType(t: GoalType) { _ui.value = _ui.value.copy(goalType = t).recalc() }
    fun setMealsPerDay(n: Int) { _ui.value = _ui.value.copy(mealsPerDay = n) }

    private fun GoalsUiState.recalc(): GoalsUiState {
        val w = weight ?: 70f
        val maintenance = (w * 30f).roundToInt() // простая оценка
        val wg = weeklyGoalKg ?: when (goalType) {
            GoalType.LOSS -> -0.5f
            GoalType.GAIN -> 0.3f
            GoalType.MAINTAIN -> 0f
        }
        val deltaPerDay = (wg * 7700f / 7f).roundToInt() // ккал/день
        val dailyTarget = (maintenance + deltaPerDay).coerceAtLeast(1200) // нижний предел
        return copy(maintenance = maintenance, dailyTarget = dailyTarget)
    }

    fun save(onDone: () -> Unit, onError: (Throwable)->Unit) {
        viewModelScope.launch {
            try {
                val st = _ui.value
                val entity = GoalEntity(
                    id = 1,
                    currentWeight = st.weight ?: 70f,
                    targetWeight = st.targetWeight ?: (st.weight ?: 70f),
                    weeklyGoal = st.weeklyGoalKg ?: 0f,
                    timeframeWeeks = st.timeframeWeeks ?: 4,
                    dailyCalories = st.dailyTarget,
                    mealsPerDay = st.mealsPerDay,
                    goalType = st.goalType.server
                )
                goalDao.saveGoal(entity)
                onDone()
            } catch (t: Throwable) {
                onError(t)
            }
        }
    }

    fun resetToDefault() {
        _ui.value = GoalsUiState(
            weight = 70f, targetWeight = 70f, weeklyGoalKg = 0f, timeframeWeeks = 4,
            goalType = GoalType.MAINTAIN, mealsPerDay = 5
        ).recalc()
    }
}
