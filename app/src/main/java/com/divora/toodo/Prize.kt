package com.divora.toodo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prizes")
data class Prize(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val cost: Int
)
