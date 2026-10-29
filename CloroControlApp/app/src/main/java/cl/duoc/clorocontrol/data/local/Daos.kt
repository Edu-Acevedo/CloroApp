package cl.duoc.clorocontrol.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements ORDER BY measuredAt DESC")
    fun observeAll(): Flow<List<MeasurementEntity>>

    @Query("SELECT COUNT(*) FROM measurements")
    suspend fun count(): Int

    @Insert
    suspend fun insert(measurement: MeasurementEntity): Long
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): AlertEntity?

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

    @Insert
    suspend fun insert(action: CorrectiveActionEntity): Long

    @Update
    suspend fun update(action: CorrectiveActionEntity)
}
