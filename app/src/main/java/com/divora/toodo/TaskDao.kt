package com.divora.toodo

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): LiveData<List<Task>>

    @Insert
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Query("SELECT SUM(points) FROM tasks WHERE isCompleted = 1")
    fun getTotalPoints(): LiveData<Int>
}
