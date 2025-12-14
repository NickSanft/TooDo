package com.divora.toodo

import androidx.lifecycle.LiveData
import javax.inject.Inject

class PointLedgerRepository @Inject constructor(private val pointLedgerDao: PointLedgerDao) {

    val allLedgerEntries: LiveData<List<PointLedger>> = pointLedgerDao.getAllLedgerEntries()

    suspend fun insert(ledgerEntry: PointLedger) {
        pointLedgerDao.insert(ledgerEntry)
    }

    suspend fun deleteAll() {
        pointLedgerDao.deleteAll()
    }
}
