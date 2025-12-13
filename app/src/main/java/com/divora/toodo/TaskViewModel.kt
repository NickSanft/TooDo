package com.divora.toodo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    private val pointLedgerRepository: PointLedgerRepository

    val allTasks: LiveData<List<Task>>
    val totalPoints: LiveData<Int>

    init {
        val appDatabase = AppDatabase.getDatabase(application)
        val taskDao = appDatabase.taskDao()
        repository = TaskRepository(taskDao)
        allTasks = repository.allTasks
        totalPoints = repository.totalPoints

        val pointLedgerDao = appDatabase.pointLedgerDao()
        pointLedgerRepository = PointLedgerRepository(pointLedgerDao)
    }

    fun insert(task: Task) = viewModelScope.launch {
        repository.insert(task)
    }

    fun update(task: Task) = viewModelScope.launch {
        val taskWithTimestamp = if (task.isCompleted) {
            if (task.completedAt == null) { // Only add to ledger if it's a new completion
                val ledgerEntry = PointLedger(
                    description = "Completed: ${task.title}",
                    points = task.points,
                    timestamp = System.currentTimeMillis()
                )
                pointLedgerRepository.insert(ledgerEntry)
            }
            task.copy(completedAt = System.currentTimeMillis())
        } else {
            task.copy(completedAt = null)
        }
        repository.update(taskWithTimestamp)
    }

    fun delete(task: Task) = viewModelScope.launch {
        repository.delete(task)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}
