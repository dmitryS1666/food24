package com.food24.track.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.food24.track.data.dao.ShoppingDao

class ShoppingListViewModelFactory(
    private val dao: ShoppingDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShoppingListViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
