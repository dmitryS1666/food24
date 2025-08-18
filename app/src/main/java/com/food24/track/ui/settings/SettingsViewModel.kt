package com.food24.track.ui.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.food24.track.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    private val _notificationsEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_NOTIFICATIONS, true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

    fun setNotifications(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()
        _notificationsEnabled.value = enabled
    }

    fun toggleNotifications() = setNotifications(!(_notificationsEnabled.value))

    /** Полная очистка локальных данных: Room + prefs приложения */
    fun clearAllData(onDone: (() -> Unit)? = null, onError: ((Throwable) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    AppDatabase.getDatabase(getApplication()).clearAllTables()
                    // если есть другие prefs — почисти их тоже:
                    getApplication<Application>()
                        .getSharedPreferences("UserData", Context.MODE_PRIVATE)
                        .edit().clear().apply()
                }
                onDone?.invoke()
            } catch (t: Throwable) {
                onError?.invoke(t)
            }
        }
    }

    companion object {
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
    }
}
