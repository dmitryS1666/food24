package com.food24.track.ui.progress

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food24.track.data.dao.DailyPlanDao
import com.food24.track.data.dao.GoalDao
import com.food24.track.data.dao.MealEntryDao
import com.food24.track.data.dao.WeightLogDao
import com.food24.track.data.entity.WeightLogEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

data class ProgressUi(
    val weights: List<Pair<LocalDate, Float>> = emptyList(),
    val current: Float? = null,
    val target: Float? = null,
    val progressText: String = "",
    val avg7days: Int = 0,
    val hits7days: Int = 0,
    val goalKcal: Int = 0,
    val bars: List<Float> = emptyList(),
    val targetLow: Float = 0f,
    val targetHigh: Float = 0f
)

class ProgressViewModel(
    private val weightDao: WeightLogDao,
    private val goalDao: GoalDao,
    private val dailyPlanDao: DailyPlanDao,   // пока не используем, но оставил на будущее
    private val mealEntryDao: MealEntryDao
) : ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    private val ISO = DateTimeFormatter.ISO_LOCAL_DATE

    @RequiresApi(Build.VERSION_CODES.O)
    val ui: StateFlow<ProgressUi> = combine(
        weightDao.observeAll(),          // Flow<List<WeightLogEntity>>
        goalDao.observeGoal(),           // Flow<GoalEntity?>
        mealEntryDao.observeLastNDays(30) // Flow<List<DailyConsumed>>
    ) { weights, goal, lastEntries ->
        // ----- вес -----
        val wSorted: List<Pair<LocalDate, Float>> =
            weights.map { LocalDate.parse(it.date, ISO) to it.weightKg }
                .sortedBy { it.first }

        val cur = wSorted.lastOrNull()?.second
        val tgt = goal?.targetWeight

        val progressText = if (wSorted.size >= 2) {
            val first = wSorted.first()
            val last  = wSorted.last()
            val days  = ChronoUnit.DAYS.between(first.first, last.first).coerceAtLeast(1)
            val delta = last.second - first.second
            val weeks = (days / 7.0).coerceAtLeast(1.0)
            "Progress: ${"%.1f".format(delta)} kg in ${"%.0f".format(weeks)} weeks"
        } else "Progress: —"

        // ----- калории -----
        val entriesSorted = lastEntries.sortedBy { it.date }
        val last7 = entriesSorted.takeLast(7)
        val totals7 = last7.map { it.consumedKcal }
        val avg = if (totals7.isNotEmpty()) totals7.average().roundToInt() else 0

        val goalKcal = goal?.dailyCalories ?: 0
        val low  = if (goalKcal > 0) (goalKcal - 200).coerceAtLeast(0) else 0
        val high = if (goalKcal > 0) goalKcal + 200 else 0
        val hits = if (goalKcal > 0) totals7.count { it in low..high } else 0

        ProgressUi(
            weights = wSorted,
            current = cur,
            target  = tgt,
            progressText = progressText,
            avg7days = avg,
            hits7days = hits,
            goalKcal = goalKcal,
            bars = totals7.map { it.toFloat() },
            targetLow = low.toFloat(),
            targetHigh = high.toFloat()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressUi())

    fun addWeight(date: LocalDate, kg: Float) {
        viewModelScope.launch {
            weightDao.insert(WeightLogEntity(date = date.toString(), weightKg = kg))
        }
    }

    fun reset() {
        viewModelScope.launch { weightDao.clear() }
    }
}
