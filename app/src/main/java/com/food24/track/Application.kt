package com.food24.track

import android.app.Application
import com.food24.track.data.database.AppDatabase

class App : Application() {
    val db: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
