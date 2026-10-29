package com.kintsu.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kintsu.app.data.model.TaskCompletionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskCompletionDao {
    @Query("SELECT * FROM task_completions ORDER BY completedAt DESC")
    fun getAllCompletions(): Flow<List<TaskCompletionEntity>>

    @Query("SELECT * FROM task_completions WHERE taskId = :taskId ORDER BY completedAt DESC")
    fun getCompletionsForTask(taskId: Long): Flow<List<TaskCompletionEntity>>

    @Query("SELECT * FROM task_completions WHERE epochDay = :epochDay")
    fun getCompletionsForDay(epochDay: Long): Flow<List<TaskCompletionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: TaskCompletionEntity): Long

    @Delete
    suspend fun deleteCompletion(completion: TaskCompletionEntity)

    @Query("DELETE FROM task_completions WHERE taskId = :taskId")
    suspend fun deleteCompletionsForTask(taskId: Long)

    @Query("DELETE FROM task_completions WHERE taskId = :taskId AND epochDay = :epochDay")
    suspend fun deleteCompletionForTaskDay(taskId: Long, epochDay: Long)

    @Query("DELETE FROM task_completions")
    suspend fun clearAllCompletions()
}
