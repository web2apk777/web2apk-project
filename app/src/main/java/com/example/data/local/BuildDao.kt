package com.example.data.local

import androidx.room.*
import com.example.data.model.BuildEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildDao {
    @Query("SELECT * FROM builds ORDER BY startedAt DESC")
    fun getAllBuilds(): Flow<List<BuildEntity>>

    @Query("SELECT * FROM builds WHERE id = :id")
    suspend fun getBuildById(id: String): BuildEntity?

    @Query("SELECT * FROM builds WHERE projectId = :projectId ORDER BY startedAt DESC")
    fun getBuildsForProject(projectId: String): Flow<List<BuildEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuild(build: BuildEntity)

    @Update
    suspend fun updateBuild(build: BuildEntity)

    @Delete
    suspend fun deleteBuild(build: BuildEntity)

    @Query("DELETE FROM builds WHERE id = :id")
    suspend fun deleteBuildById(id: String)
}
