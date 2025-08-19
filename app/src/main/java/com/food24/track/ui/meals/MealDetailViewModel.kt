package com.food24.track.ui.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food24.track.data.dao.MealDao
import com.food24.track.data.dao.MealEntryDao
import com.food24.track.data.entity.MealEntryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.first

class MealDetailViewModel(
    private val mealDao: MealDao,
    private val mealEntryDao: MealEntryDao
) : ViewModel() {

    private val _ui = MutableStateFlow(MealDetailUi())
    val ui: StateFlow<MealDetailUi> = _ui

    private var date: String = ""
    private var type: String = ""

    fun init(date: String, mealType: String) {
        this.date = date
        this.type = mealType
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val entries = mealEntryDao.getEntriesByDate(date).first()
            val own = entries.filter { e ->
                val m = mealDao.getById(e.mealId)
                m?.type == type
            }

            val uiItems = own.mapNotNull { e ->
                val m = mealDao.getById(e.mealId) ?: return@mapNotNull null
                UiFood(
                    mealId = m.id,
                    title = m.name,
                    kcal = m.calories,
                    p = m.protein, f = m.fat, c = m.carbs
                )
            }

            val totalKcal = uiItems.sumOf { it.kcal }
            val totalP = uiItems.sumOf { it.p }
            val totalF = uiItems.sumOf { it.f }
            val totalC = uiItems.sumOf { it.c }

            _ui.value = MealDetailUi(
                mealType = type,
                items = uiItems,
                totalKcal = totalKcal,
                protein = totalP,
                fat = totalF,
                carbs = totalC
            )
        }
    }

    fun delete(mealId: Int) {
        viewModelScope.launch {
            mealEntryDao.deleteEntry(date, mealId)
            refresh()
        }
    }

    fun swap(mealId: Int) {
        viewModelScope.launch {
            val pool = mealDao.getMealsByType(type).filter { it.id != mealId }
            val replacement = pool.randomOrNull() ?: return@launch
            mealEntryDao.deleteEntry(date, mealId)
            mealEntryDao.insertEntry(MealEntryEntity(date = date, mealId = replacement.id, eaten = false))
            refresh()
        }
    }
}

/* UI-модель экрана */
data class MealDetailUi(
    val mealType: String = "",
    val items: List<UiFood> = emptyList(),
    val totalKcal: Int = 0,
    val protein: Int = 0,
    val fat: Int = 0,
    val carbs: Int = 0
) {
    fun proteinPct(target: Int) = pct(protein, target)
    fun fatPct(target: Int) = pct(fat, target)
    fun carbPct(target: Int) = pct(carbs, target)
    private fun pct(v: Int, t: Int) = if (t <= 0) 0 else (v * 100f / t).roundToInt().coerceIn(0, 100)
}

data class UiFood(
    val mealId: Int,
    val title: String,
    val kcal: Int,
    val p: Int,
    val f: Int,
    val c: Int
)
