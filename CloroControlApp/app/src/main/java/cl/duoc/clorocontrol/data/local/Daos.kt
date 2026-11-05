package cl.duoc.clorocontrol.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements ORDER BY measuredAt DESC")
    fun observeAll(): Flow<List<MeasurementEntity>>

    @Query("SELECT COUNT(*) FROM measurements")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM measurements WHERE synced = 0")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT * FROM measurements WHERE synced = 0 ORDER BY measuredAt ASC")
    suspend fun pendingForSync(): List<MeasurementEntity>

    @Query("SELECT * FROM measurements WHERE clientId = :clientId LIMIT 1")
    suspend fun findByClientId(clientId: String): MeasurementEntity?

    @Query("SELECT * FROM measurements WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): MeasurementEntity?

    @Query("UPDATE measurements SET synced = 1 WHERE clientId = :clientId")
    suspend fun markSyncedByClientId(clientId: String)

    @Insert
    suspend fun insert(measurement: MeasurementEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRemote(measurement: MeasurementEntity): Long
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): AlertEntity?

    @Query("SELECT * FROM alerts WHERE measurementId = :measurementId LIMIT 1")
    suspend fun findByMeasurementId(measurementId: Long): AlertEntity?

    @Query("""
        SELECT alerts.id FROM alerts
        INNER JOIN measurements ON measurements.id = alerts.measurementId
        WHERE measurements.clientId = :measurementClientId
        LIMIT 1
    """)
    suspend fun findAlertIdByMeasurementClientId(measurementClientId: String): Long?

    @Insert
    suspend fun insert(alert: AlertEntity): Long

    @Update
    suspend fun update(alert: AlertEntity)
}

@Dao
interface CorrectiveActionDao {
    @Query("SELECT * FROM corrective_actions ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<CorrectiveActionEntity>>

    @Query("SELECT * FROM corrective_actions WHERE alertId = :alertId ORDER BY createdAt DESC LIMIT 1")
    suspend fun latestForAlert(alertId: Long): CorrectiveActionEntity?

    @Query("SELECT * FROM corrective_actions WHERE clientId = :clientId LIMIT 1")
    suspend fun findByClientId(clientId: String): CorrectiveActionEntity?

    @Query("UPDATE corrective_actions SET synced = 1 WHERE clientId = :clientId")
    suspend fun markSyncedByClientId(clientId: String)

    @Query("SELECT * FROM corrective_actions WHERE synced = 0 ORDER BY createdAt ASC")
    suspend fun pendingForSync(): List<CorrectiveActionEntity>

    @Insert
    suspend fun insert(action: CorrectiveActionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRemote(action: CorrectiveActionEntity): Long

    @Update
    suspend fun update(action: CorrectiveActionEntity)
}

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC, id ASC")
    suspend fun pending(): List<SyncQueueEntity>

    @Query("SELECT COUNT(*) FROM sync_queue")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: SyncQueueEntity): Long

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE sync_queue SET attempts = attempts + 1, lastError = :message WHERE id = :id")
    suspend fun recordFailure(id: Long, message: String)
}
