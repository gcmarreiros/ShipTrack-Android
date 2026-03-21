package com.shiptrack

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY created DESC")
    fun getAllLive(): LiveData<List<Task>>
    @Query("SELECT * FROM tasks ORDER BY created DESC")
    suspend fun getAll(): List<Task>
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: String): Task?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)
    @Update
    suspend fun update(task: Task)
    @Delete
    suspend fun delete(task: Task)
    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)
    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun count(): Int
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(tasks: List<Task>)
}
