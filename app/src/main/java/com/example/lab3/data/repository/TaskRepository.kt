package com.example.lab3.data.repository

import com.example.lab3.data.local.TaskDao
import com.example.lab3.data.local.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    val tasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    fun getTaskById(id: Int): Flow<TaskEntity?> = taskDao.getTaskById(id)

    suspend fun addTask(task: TaskEntity) = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)
}
