package com.example.data.repository

import com.example.data.db.ExportRecordDao
import com.example.data.model.ExportRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ExportRecordRepository(
    private val exportRecordDao: ExportRecordDao
) {
    val allExportRecords: Flow<List<ExportRecordEntity>> = exportRecordDao.getAllExportRecords()

    suspend fun recordExport(record: ExportRecordEntity) = withContext(Dispatchers.IO) {
        exportRecordDao.insertExportRecord(record)
    }

    suspend fun deleteExportRecord(record: ExportRecordEntity) = withContext(Dispatchers.IO) {
        exportRecordDao.deleteExportRecord(record)
    }

    suspend fun clearExportHistory() = withContext(Dispatchers.IO) {
        exportRecordDao.clearExportHistory()
    }
}
