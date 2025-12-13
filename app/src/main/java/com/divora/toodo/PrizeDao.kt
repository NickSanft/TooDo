package com.divora.toodo

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PrizeDao {
    @Query("SELECT * FROM prizes")
    fun getAllPrizes(): LiveData<List<Prize>>

    @Insert
    suspend fun insert(prize: Prize)

    @Update
    suspend fun update(prize: Prize)

    @Delete
    suspend fun delete(prize: Prize)

    @Query("DELETE FROM prizes")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM prizes")
    suspend fun getCount(): Int
}
