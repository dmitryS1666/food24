package com.food24.track.ui.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.food24.track.data.database.AppDatabase
import com.food24.track.data.entity.ProgressEntryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

class ProgressViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.getDatabase(app).progressDao()

    private val _progress = MutableStateFlow<List<ProgressEntryEntity>>(emptyList())
    val progress: StateFlow<List<ProgressEntryEntity>> = _progress

    fun loadProgress() {
        viewModelScope.launch {
            dao.observeAll()            // Flow<List<ProgressEntryEntity>>
                .collectLatest { _progress.value = it }
        }
    }

    fun addEntry(e: ProgressEntryEntity) {
        viewModelScope.launch { dao.insert(e) }
    }

    fun resetProgress() {
        viewModelScope.launch { dao.clearAll() }
    }
}
