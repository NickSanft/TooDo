package com.divora.toodo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
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

    private val _searchQuery = MutableLiveData<String>("")
    private val _sortOrder = MutableLiveData<SortOrder>(SortOrder.PRIORITY)
    
    val allTasks: LiveData<List<Task>> = repository.allTasks
    val totalPoints: LiveData<Int> = repository.totalPoints

    val filteredTasks = MediatorLiveData<List<Task>>().apply {
        fun update() {
            val tasks = allTasks.value ?: emptyList()
            val query = _searchQuery.value ?: ""
            val sort = _sortOrder.value ?: SortOrder.PRIORITY

            val filtered = tasks.filter { 
                it.title.contains(query, ignoreCase = true) 
            }

            val sorted = when (sort) {
                SortOrder.PRIORITY -> filtered.sortedBy { it.priority }
                SortOrder.POINTS -> filtered.sortedByDescending { it.points }
                SortOrder.AZ -> filtered.sortedBy { it.title }
                SortOrder.NEWEST -> filtered.sortedByDescending { it.id }
            }
            
            value = sorted
        }

        addSource(allTasks) { update() }
        addSource(_searchQuery) { update() }
        addSource(_sortOrder) { update() }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(sortOrder: SortOrder) {
        _sortOrder.value = sortOrder
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

enum class SortOrder {
    PRIORITY, POINTS, AZ, NEWEST
}
