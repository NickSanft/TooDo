package com.divora.toodo

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrizesViewModel @Inject constructor(
    private val repository: PrizeRepository
) : ViewModel() {

    val prizes: LiveData<List<Prize>> = repository.allPrizes

    init {
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
