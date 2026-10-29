package cl.duoc.clorocontrol.data.repository

import androidx.room.withTransaction
import cl.duoc.clorocontrol.data.local.AlertEntity
import cl.duoc.clorocontrol.data.local.CloroControlDatabase
import cl.duoc.clorocontrol.data.local.CorrectiveActionEntity
import cl.duoc.clorocontrol.data.local.MeasurementEntity
import cl.duoc.clorocontrol.domain.ChlorineRules
import cl.duoc.clorocontrol.domain.ChlorineStatus
import cl.duoc.clorocontrol.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ChlorineRepository(private val database: CloroControlDatabase) {
    private val seedMutex = Mutex()

    val measurements: Flow<List<MeasurementEntity>> = database.measurementDao().observeAll()
    val alerts: Flow<List<AlertEntity>> = database.alertDao().observeAll()
    val actions: Flow<List<CorrectiveActionEntity>> = database.correctiveActionDao().observeAll()

    suspend fun ensureSeedData() = seedMutex.withLock {
        if (database.measurementDao().count() > 0) return@withLock
        SimulatedChlorineApi.fetchInitialMeasurements().forEach { draft ->
            val time = System.currentTimeMillis() - draft.ageMinutes * 60_000L
            saveMeasurement(
                farm = draft.farm,
                shed = draft.shed,
                pointCode = draft.pointCode,
                location = draft.location,
                chlorinePpm = draft.chlorinePpm,
                createdBy = "datos.demo@clorocontrol.cl",
                measuredAt = time
            )
        }
    }

    suspend fun saveMeasurement(
        farm: String,
        shed: String,
        pointCode: String,
        location: String,
        chlorinePpm: Double,
        createdBy: String,
        measuredAt: Long = System.currentTimeMillis()
    ) {
        val status = ChlorineRules.status(chlorinePpm)
        database.withTransaction {
            val id = database.measurementDao().insert(
                MeasurementEntity(
                    farm = farm.trim(),
                    shed = shed.trim(),
                    pointCode = pointCode.trim(),
                    location = location.trim(),
                    chlorinePpm = chlorinePpm,
                    measuredAt = measuredAt,
                    status = status.name,
                    createdBy = createdBy
                )
            )
            if (status != ChlorineStatus.NORMAL) {
                database.alertDao().insert(
                    AlertEntity(
                        measurementId = id,
                        farm = farm.trim(),
                        pointCode = pointCode.trim(),
                        chlorinePpm = chlorinePpm,
                        issue = ChlorineRules.issue(chlorinePpm),
                        severity = ChlorineRules.severity(chlorinePpm),
                        createdAt = measuredAt
                    )
                )
            }
        }
    }

    suspend fun registerCorrectiveAction(
        alertId: Long,
        description: String,
        responsible: String,
        verifiedPpm: Double,
        evidenceSource: String,
        evidenceUri: String?,
        role: UserRole
    ): Boolean {
        if (role == UserRole.JEFATURA) return false
        return database.withTransaction {
            val alert = database.alertDao().findById(alertId) ?: return@withTransaction false
            val canValidateNow = role == UserRole.SUPERVISOR && ChlorineRules.canCloseAlert(verifiedPpm)
            database.correctiveActionDao().insert(
                CorrectiveActionEntity(
                    alertId = alertId,
                    description = description.trim(),
                    responsible = responsible,
                    createdAt = System.currentTimeMillis(),
                    verifiedPpm = verifiedPpm,
                    evidenceSource = evidenceSource,
                    evidenceUri = evidenceUri,
                    status = if (canValidateNow) "VALIDADA" else "PENDIENTE_VALIDACION",
                    validatedBy = if (canValidateNow) responsible else null
                )
            )
            if (canValidateNow) database.alertDao().update(alert.copy(status = "CERRADA"))
            true
        }
    }

    suspend fun validateLatestAction(alertId: Long, supervisorEmail: String, role: UserRole): ValidationResult {
        if (role != UserRole.SUPERVISOR) {
            return ValidationResult(false, "Solo un Supervisor puede validar y cerrar alertas.")
        }
        return database.withTransaction {
            val alert = database.alertDao().findById(alertId)
                ?: return@withTransaction ValidationResult(false, "No se encontró la alerta.")
            val action = database.correctiveActionDao().latestForAlert(alertId)
                ?: return@withTransaction ValidationResult(false, "Primero registra una acción correctiva.")
            if (!ChlorineRules.canCloseAlert(action.verifiedPpm)) {
                return@withTransaction ValidationResult(
                    false,
                    "La verificación debe quedar entre 3,00 y 5,00 ppm para cerrar la alerta."
                )
            }
            database.correctiveActionDao().update(
                action.copy(status = "VALIDADA", validatedBy = supervisorEmail)
            )
            database.alertDao().update(alert.copy(status = "CERRADA"))
            ValidationResult(true, "Acción validada y alerta cerrada.")
        }
    }
}

data class ValidationResult(val success: Boolean, val message: String)
