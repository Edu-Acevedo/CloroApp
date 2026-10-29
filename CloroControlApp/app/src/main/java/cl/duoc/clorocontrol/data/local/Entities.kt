package cl.duoc.clorocontrol.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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

@Entity(tableName = "corrective_actions")
data class CorrectiveActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alertId: Long,
    val description: String,
    val responsible: String,
    val createdAt: Long,
    val verifiedPpm: Double,
    val evidenceSource: String = "Sin evidencia",
    val evidenceUri: String? = null,
    val status: String = "PENDIENTE_VALIDACION",
    val validatedBy: String? = null
)
