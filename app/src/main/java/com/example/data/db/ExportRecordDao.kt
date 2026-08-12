package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ExportRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExportRecordDao {
    @Query("SELECT * FROM export_history ORDER BY exportedAt DESC")
    fun getAllExportRecords(): Flow<List<ExportRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExportRecord(record: ExportRecordEntity)

    @Delete
    suspend fun deleteExportRecord(record: ExportRecordEntity)

    @Query("DELETE FROM export_history WHERE id = :id")
    suspend fun deleteExportRecordById(id: String)

    @Query("DELETE FROM export_history")
    suspend fun clearExportHistory()
}
