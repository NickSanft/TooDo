package com.divora.toodo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "point_ledger")
data class PointLedger(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val points: Int,
    val timestamp: Long
)
