package com.food24.track.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.food24.track.data.dao.ParticipantDao
import com.food24.track.data.database.AppDatabase
import com.food24.track.data.repository.DataRepository

class DashboardViewModel(
    application: Application,
    private val dataRepository: DataRepository
) : AndroidViewModel(application) {
    private val repository: EventRepository

    val lastEvent: LiveData<EventEntity?>

    val teams: LiveData<List<TeamEntity>>

    val allEvents: LiveData<List<EventEntity>>
        get() = this.repository.getAllEvents().asLiveData()

    init {
        val db = AppDatabase.getDatabase(application)

        val eventDao = db.eventDao()
        val participantDao = db.participantDao()
        val teamParticipantDao = db.teamParticipantDao()
        val liveResultDao = db.liveResultDao()

        val teamDao = db.teamDao()

        repository = EventRepository(eventDao, participantDao, teamParticipantDao, liveResultDao)

        teams = teamDao.getAllTeams().asLiveData()

        lastEvent = repository.getAllEvents().map { it.firstOrNull() }.asLiveData()
    }

    fun deleteEvent(id: Int) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }

    fun getEventById(id: Int): LiveData<EventEntity> {
        return repository.getById(id)
    }

    fun updateEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.update(event)
        }
    }

    fun getParticipantsByEventId(eventId: Int): LiveData<List<ParticipantEntity>> {
        return repository.getParticipantsByEventId(eventId)
    }

    fun insertParticipant(participant: ParticipantEntity) {
        viewModelScope.launch {
            repository.insertParticipant(participant)
        }
    }

    fun deleteParticipantsByEventId(eventId: Int) {
        viewModelScope.launch {
            repository.deleteParticipantsByEventId(eventId)
        }
    }

    val participantCountsByEvent: LiveData<Map<Int, Int>> =
        repository.getParticipantCountsByEvent()
            .map { list -> list.associate { it.eventId to it.count } }
            .asLiveData()

    fun getParticipantsForEvent(eventId: Int): LiveData<List<ParticipantEntity>> {
        return repository.getParticipantsByEventId(eventId)
    }

    fun clearAllData() {
        viewModelScope.launch {
            dataRepository.clearAllData()
        }
    }

    suspend fun getEventByIdOnce(id: Int): EventEntity? {
        return withContext(Dispatchers.IO) {
            repository.getEventByIdNow(id)
        }
    }

    suspend fun getParticipantsByEventIdOnce(eventId: Int): List<ParticipantEntity> {
        return repository.getParticipantsByEventIdOnce(eventId)
    }
}
