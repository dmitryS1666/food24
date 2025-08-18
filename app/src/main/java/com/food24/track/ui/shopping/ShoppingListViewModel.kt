package com.food24.track.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food24.track.data.dao.ShoppingDao
import com.food24.track.data.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShoppingListViewModel(
    private val shoppingDao: ShoppingDao
) : ViewModel() {

    private val _items = MutableStateFlow<List<ShoppingItemEntity>>(emptyList())
    val items: StateFlow<List<ShoppingItemEntity>> = _items

    fun loadList() {
        viewModelScope.launch {
            _items.value = shoppingDao.getAllItems()
        }
    }

    fun toggleItem(item: ShoppingItemEntity) {
        viewModelScope.launch {
            shoppingDao.updateItem(item.copy(checked = !item.checked))
            loadList()
        }
    }
}
