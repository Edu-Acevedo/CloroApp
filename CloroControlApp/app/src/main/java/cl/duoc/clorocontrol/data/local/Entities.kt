package cl.duoc.clorocontrol.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "measurements",
    indices = [Index(value = ["clientId"], unique = true)]
)
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(defaultValue = "''") val clientId: String = UUID.randomUUID().toString(),
    val farm: String,
    val shed: String,
    val pointCode: String,
    val location: String,
    val chlorinePpm: Double,
    val measuredAt: Long,
    val status: String,
    val createdBy: String,
    val synced: Boolean = false
)

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val measurementId: Long,
    val farm: String,
    val pointCode: String,
    val chlorinePpm: Double,
    val issue: String,
    val severity: String,
    val status: String = "ABIERTA",
    val createdAt: Long
)

@Entity(
    tableName = "corrective_actions",
    indices = [Index(value = ["clientId"], unique = true)]
)
data class CorrectiveActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(defaultValue = "''") val clientId: String = UUID.randomUUID().toString(),
    val alertId: Long,
    val description: String,
    val responsible: String,
    val createdAt: Long,
    val verifiedPpm: Double,
    val evidenceSource: String = "Sin evidencia",
    val evidenceUri: String? = null,
    val status: String = "PENDIENTE_VALIDACION",
    val validatedBy: String? = null,
    @ColumnInfo(defaultValue = "1") val synced: Boolean = false
)

@Entity(
    tableName = "sync_queue",
    indices = [Index(value = ["eventType", "eventClientId"], unique = true)]
)
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: String,
    val eventClientId: String,
    val payloadJson: String,
    val createdAt: Long,
    @ColumnInfo(defaultValue = "0") val attempts: Int = 0,
    val lastError: String? = null
)
