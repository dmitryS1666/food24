package com.food24.track.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun add(name: String, amount: String, category: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            dao.insert(
                ShoppingItemEntity(
                    name = name.trim(),
                    amount = amount.trim(),
                    category = category.trim(),
                    checked = false
                )
            )
        }
    }

    fun toggle(id: Int, checked: Boolean) {
        viewModelScope.launch { dao.setChecked(id, checked) }
    }

    fun clearChecked() { viewModelScope.launch { dao.clearChecked() } }
    fun resetAll() { viewModelScope.launch { dao.clearAll() } }
}
