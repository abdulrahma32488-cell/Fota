package com.example.data.local

import androidx.room.*
import com.example.model.ChatMessage
import com.example.model.Project
import com.example.model.ProjectFile
import com.example.model.VersionSnapshot
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getProjectById(projectId: String): Project?

    @Query("SELECT * FROM projects WHERE id = :projectId")
    fun observeProjectById(projectId: String): Flow<Project?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project)

    @Update
    suspend fun updateProject(project: Project)

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProject(projectId: String)

    // Project Files
    @Query("SELECT * FROM project_files WHERE projectId = :projectId ORDER BY path ASC")
    fun getFilesForProject(projectId: String): Flow<List<ProjectFile>>

    @Query("SELECT * FROM project_files WHERE projectId = :projectId ORDER BY path ASC")
    suspend fun getFilesForProjectSync(projectId: String): List<ProjectFile>

    @Query("SELECT * FROM project_files WHERE projectId = :projectId AND path = :path LIMIT 1")
    suspend fun getFileByPath(projectId: String, path: String): ProjectFile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: ProjectFile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<ProjectFile>)

    @Query("DELETE FROM project_files WHERE projectId = :projectId")
    suspend fun deleteFilesForProject(projectId: String)

    @Query("DELETE FROM project_files WHERE projectId = :projectId AND path = :path")
    suspend fun deleteFile(projectId: String, path: String)

    // Chat Messages
    @Query("SELECT * FROM chat_messages WHERE projectId = :projectId ORDER BY timestamp ASC")
    fun getMessagesForProject(projectId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE projectId = :projectId")
    suspend fun deleteMessagesForProject(projectId: String)

    // Version Snapshots
    @Query("SELECT * FROM version_snapshots WHERE projectId = :projectId ORDER BY versionNumber DESC")
    fun getSnapshotsForProject(projectId: String): Flow<List<VersionSnapshot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: VersionSnapshot)
}
