package com.divora.toodo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val difficulty: String,
    val points: Int,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)
