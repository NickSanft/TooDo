package com.divora.toodo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PrizesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PrizeRepository
    val prizes: LiveData<List<Prize>>

    init {
        val prizeDao = AppDatabase.getDatabase(application).prizeDao()
        repository = PrizeRepository(prizeDao)
        prizes = repository.allPrizes

        viewModelScope.launch(Dispatchers.IO) {
            if (repository.getCount() == 0) {
                repository.insert(Prize(name = "Movie Night", cost = 25))
                repository.insert(Prize(name = "Ice Cream", cost = 10))
                repository.insert(Prize(name = "New Book", cost = 50))
            }
        }
    }

    fun addPrize(prize: Prize) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(prize)
    }

    fun removePrize(prize: Prize) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(prize)
    }

    fun updatePrize(prize: Prize) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(prize)
    }

    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAll()
    }
}
