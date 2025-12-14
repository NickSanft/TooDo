package com.divora.toodo

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PointLedgerViewModel @Inject constructor(
    private val repository: PointLedgerRepository
) : ViewModel() {

    val allLedgerEntries: LiveData<List<PointLedger>> = repository.allLedgerEntries

    fun insert(ledgerEntry: PointLedger) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(ledgerEntry)
    }

    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAll()
    }
}
