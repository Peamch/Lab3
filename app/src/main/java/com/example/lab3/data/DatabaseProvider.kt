package com.example.lab3.data

import android.content.Context
import androidx.room.Room
import com.example.lab3.data.local.AppDatabase

object DatabaseProvider {

    @Volatile
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            val db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "tasks_database"
            ).build()
            instance = db
            db
        }
    }
}
