package com.food24.track.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food24.track.data.dao.ProgressDao
import com.food24.track.data.entity.ProgressEntryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProgressViewModel(
    private val progressDao: ProgressDao
) : ViewModel() {

    private val _progress = MutableStateFlow<List<ProgressEntryEntity>>(emptyList())
    val progress: StateFlow<List<ProgressEntryEntity>> = _progress

    fun loadProgress() {
        viewModelScope.launch {
            _progress.value = progressDao.getAllProgress()
        }
    }

    fun addEntry(entry: ProgressEntryEntity) {
        viewModelScope.launch {
            progressDao.insertProgress(entry)
            loadProgress()
        }
    }
}
