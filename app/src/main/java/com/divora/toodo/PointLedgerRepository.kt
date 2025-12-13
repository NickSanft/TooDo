package com.divora.toodo

import androidx.lifecycle.LiveData

class PointLedgerRepository(private val pointLedgerDao: PointLedgerDao) {

    val allLedgerEntries: LiveData<List<PointLedger>> = pointLedgerDao.getAllLedgerEntries()

    suspend fun insert(ledgerEntry: PointLedger) {
        pointLedgerDao.insert(ledgerEntry)
    }

    suspend fun deleteAll() {
        pointLedgerDao.deleteAll()
    }
}
