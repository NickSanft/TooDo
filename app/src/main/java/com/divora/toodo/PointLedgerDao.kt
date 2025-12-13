package com.divora.toodo

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PointLedgerDao {
    @Query("SELECT * FROM point_ledger ORDER BY timestamp DESC")
    fun getAllLedgerEntries(): LiveData<List<PointLedger>>

    @Insert
    suspend fun insert(ledgerEntry: PointLedger)

    @Query("DELETE FROM point_ledger")
    suspend fun deleteAll()
}
