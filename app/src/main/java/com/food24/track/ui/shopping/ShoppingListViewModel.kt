package com.food24.track.ui.shopping

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.food24.track.data.dao.ShoppingDao
import com.food24.track.data.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UiShoppingItem(
    val id: Int,
    val name: String,
    val amount: String,
    val category: String,
    val checked: Boolean
)

data class ShoppingUiState(
    val items: List<UiShoppingItem> = emptyList(),
    val hasChecked: Boolean = false,
    val empty: Boolean = true
)

class ShoppingListViewModel(
    private val dao: ShoppingDao
) : ViewModel() {
    companion object {
        private const val TAG = "ShoppingVM"
    }

    private val seed = listOf(
        // 🥩 Meat & Fish
        ShoppingItemEntity(name = "Chicken breast", amount = "450 g — 495 kcal", category = "Meat & Fish"),
        ShoppingItemEntity(name = "Salmon fillet",  amount = "200 g — 400 kcal", category = "Meat & Fish"),
        ShoppingItemEntity(name = "Beef steak",     amount = "250 g — 680 kcal", category = "Meat & Fish"),
        ShoppingItemEntity(name = "Tuna (canned)",  amount = "150 g — 180 kcal", category = "Meat & Fish"),

        // 🥦 Vegetables & Fruits
        ShoppingItemEntity(name = "Apple",     amount = "2 pcs — 180 kcal", category = "Vegetables & Fruits"),
        ShoppingItemEntity(name = "Banana",    amount = "1 pc — 120 kcal",  category = "Vegetables & Fruits"),
        ShoppingItemEntity(name = "Broccoli",  amount = "200 g — 70 kcal",  category = "Vegetables & Fruits"),
        ShoppingItemEntity(name = "Carrot",    amount = "150 g — 60 kcal",  category = "Vegetables & Fruits"),

        // 🌾 Grains & Carbs
        ShoppingItemEntity(name = "Brown rice",        amount = "500 g — 1800 kcal", category = "Grains & Carbs"),
        ShoppingItemEntity(name = "Oats",              amount = "100 g — 380 kcal",  category = "Grains & Carbs"),
        ShoppingItemEntity(name = "Whole-grain bread", amount = "2 slices — 150 kcal", category = "Grains & Carbs"),
        ShoppingItemEntity(name = "Pasta (dry)",       amount = "100 g — 350 kcal", category = "Grains & Carbs"),

        // 🫒 Oils & Condiments
        ShoppingItemEntity(name = "Olive oil",     amount = "100 ml — 884 kcal", category = "Oils & Condiments"),
        ShoppingItemEntity(name = "Sunflower oil", amount = "100 ml — 900 kcal", category = "Oils & Condiments"),
        ShoppingItemEntity(name = "Soy sauce",     amount = "30 ml — 15 kcal",   category = "Oils & Condiments"),
        ShoppingItemEntity(name = "Mayonnaise",    amount = "50 g — 330 kcal",   category = "Oils & Condiments"),

        // 🥛 Dairy & Eggs
        ShoppingItemEntity(name = "Eggs",           amount = "10 pcs — ~780 kcal", category = "Dairy & Eggs"),
        ShoppingItemEntity(name = "Milk",           amount = "250 ml — 150 kcal",  category = "Dairy & Eggs"),
        ShoppingItemEntity(name = "Cottage cheese", amount = "200 g — 180 kcal",   category = "Dairy & Eggs"),
        ShoppingItemEntity(name = "Cheese (hard)",  amount = "100 g — 350 kcal",   category = "Dairy & Eggs"),

        // 🥜 Others
        ShoppingItemEntity(name = "Mixed nuts",    amount = "200 g — 1200 kcal", category = "Others"),
        ShoppingItemEntity(name = "Dark chocolate",amount = "100 g — 550 kcal",  category = "Others"),
        ShoppingItemEntity(name = "Peanut butter", amount = "50 g — 300 kcal",   category = "Others"),
        ShoppingItemEntity(name = "Crackers",      amount = "100 g — 430 kcal",  category = "Others")
    )

    val ui: StateFlow<ShoppingUiState> = dao.observeAll()
        .map { list ->
            val uiItems = list.map {
                UiShoppingItem(
                    id = it.id,
                    name = it.name,
                    amount = it.amount,
                    category = it.category,
                    checked = it.checked
                )
            }
            ShoppingUiState(
                items = uiItems,
                hasChecked = uiItems.any { it.checked },
                empty = uiItems.isEmpty()
            )
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, ShoppingUiState())

    @OptIn(UnstableApi::class)
    fun debugDump(prefix: String = "") {
        viewModelScope.launch {
            val items = dao.getAllOnce()
            Log.d(TAG, "debugDump$prefix: items size=${items.size}")
            items.forEach {
                Log.d(TAG, "  item: id=${it.id}, name='${it.name}', amount='${it.amount}', cat='${it.category}', checked=${it.checked}")
            }

            val counts = dao.countsByCategory()
            Log.d(TAG, "debugDump$prefix: category counts:")
            counts.forEach { c ->
                Log.d(TAG, "  ${c.category} -> ${c.cnt}")
            }

            val names = items.map { it.name.trim().lowercase() }.toSet()
            val cats  = items.map { it.category.trim() }.toSet()
            Log.d(TAG, "debugDump$prefix: uniqueNames=${names.size} $names, uniqueCats=${cats.size} $cats")
        }
    }

    @OptIn(UnstableApi::class)
    fun normalizeIfAllSameName() {
        viewModelScope.launch {
            Log.d(TAG, "normalizeIfAllSameName: START")
            val items = dao.getAllOnce()
            Log.d(TAG, "normalizeIfAllSameName: snapshot size=${items.size}")
            items.forEach {
                Log.d(TAG, "  snapshot item: id=${it.id}, name='${it.name}', cat='${it.category}'")
            }

            if (items.isEmpty()) {
                Log.d(TAG, "normalizeIfAllSameName: EMPTY -> reseed")
                dao.clearAll()
                dao.insertItems(seed)
                debugDump(" [after reseed empty]")
                return@launch
            }

            val names = items.map { it.name.trim().lowercase() }.toSet()
            val cats  = items.map { it.category.trim() }.toSet()
            Log.d(TAG, "normalizeIfAllSameName: uniqueNames=${names.size}, uniqueCats=${cats.size}")

            if (names.size == 1 && cats.size >= 3) {
                Log.d(TAG, "normalizeIfAllSameName: all same name across many cats -> reseed")
                dao.clearAll()
                dao.insertItems(seed)
                debugDump(" [after reseed same-name]")
                return@launch
            }

            val required = setOf(
                "Meat & Fish", "Vegetables & Fruits", "Grains & Carbs",
                "Oils & Condiments", "Dairy & Eggs", "Others"
            )
            val catCounts = dao.countsByCategory().associate { it.category.trim() to it.cnt }
            Log.d(TAG, "normalizeIfAllSameName: catCounts=$catCounts")

            val badDistribution = required.any { cat -> (catCounts[cat] ?: 0) == 0 }
            if (badDistribution) {
                Log.d(TAG, "normalizeIfAllSameName: bad distribution (missing required cat) -> reseed")
                dao.clearAll()
                dao.insertItems(seed)
                debugDump(" [after reseed bad-dist]")
            } else {
                Log.d(TAG, "normalizeIfAllSameName: distribution OK, no reseed")
                debugDump(" [no reseed]")
            }
        }
    }

    fun forceReseed() {
        viewModelScope.launch {
            dao.clearAll()
            dao.insertItems(seed)
        }
    }

    fun add(name: String, amount: String, category: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val canonical = Categories.canonicalOrOthers(category)
            dao.insert(
                ShoppingItemEntity(
                    name = name.trim(),
                    amount = amount.trim(),
                    category = canonical,
                    checked = false
                )
            )
        }
    }

    fun toggle(id: Int, checked: Boolean) {
        viewModelScope.launch { dao.setChecked(id, checked) }
    }

    fun clearChecked() { viewModelScope.launch { dao.clearChecked() } }
}
