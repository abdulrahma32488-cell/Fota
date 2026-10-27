package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.ChatMessage
import com.example.model.Project
import com.example.model.ProjectFile
import com.example.model.VersionSnapshot

@Database(
    entities = [
        Project::class,
        ProjectFile::class,
        ChatMessage::class,
        VersionSnapshot::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FlotaDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile
        private var INSTANCE: FlotaDatabase? = null

        fun getDatabase(context: Context): FlotaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FlotaDatabase::class.java,
                    "flota_projects.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
