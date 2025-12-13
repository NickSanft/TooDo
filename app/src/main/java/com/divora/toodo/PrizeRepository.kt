package com.divora.toodo

import androidx.lifecycle.LiveData

class PrizeRepository(private val prizeDao: PrizeDao) {

    val allPrizes: LiveData<List<Prize>> = prizeDao.getAllPrizes()

    suspend fun insert(prize: Prize) {
        prizeDao.insert(prize)
    }

    suspend fun update(prize: Prize) {
        prizeDao.update(prize)
    }

    suspend fun delete(prize: Prize) {
        prizeDao.delete(prize)
    }

    suspend fun deleteAll() {
        prizeDao.deleteAll()
    }

    suspend fun getCount(): Int {
        return prizeDao.getCount()
    }
}
