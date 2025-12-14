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
    val priority: Int = 2, // 1 = High, 2 = Medium, 3 = Low
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)
