package com.divora.toodo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PointLedgerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PointLedgerRepository
    val allLedgerEntries: LiveData<List<PointLedger>>

    init {
        val pointLedgerDao = AppDatabase.getDatabase(application).pointLedgerDao()
        repository = PointLedgerRepository(pointLedgerDao)
        allLedgerEntries = repository.allLedgerEntries
    }

    fun insert(ledgerEntry: PointLedger) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(ledgerEntry)
    }

    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAll()
    }
}
