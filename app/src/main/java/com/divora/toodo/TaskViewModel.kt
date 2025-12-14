package com.divora.toodo

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val pointLedgerRepository: PointLedgerRepository
) : ViewModel() {

    val allTasks: LiveData<List<Task>> = repository.allTasks
    val totalPoints: LiveData<Int> = repository.totalPoints

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
