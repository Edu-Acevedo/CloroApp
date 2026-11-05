package cl.duoc.clorocontrol.data.repository

import android.content.Context
import androidx.room.withTransaction
import cl.duoc.clorocontrol.data.local.AlertEntity
import cl.duoc.clorocontrol.data.local.CloroControlDatabase
import cl.duoc.clorocontrol.data.local.CorrectiveActionEntity
import cl.duoc.clorocontrol.data.local.MeasurementEntity
import cl.duoc.clorocontrol.data.local.SyncQueueEntity
import cl.duoc.clorocontrol.data.sync.SyncScheduler
import cl.duoc.clorocontrol.domain.ChlorineRules
import cl.duoc.clorocontrol.domain.ChlorineStatus
import cl.duoc.clorocontrol.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.util.UUID

class ChlorineRepository(
    private val database: CloroControlDatabase,
    private val appContext: Context
) {
    private val seedMutex = Mutex()

    val measurements: Flow<List<MeasurementEntity>> = database.measurementDao().observeAll()
    val alerts: Flow<List<AlertEntity>> = database.alertDao().observeAll()
    val actions: Flow<List<CorrectiveActionEntity>> = database.correctiveActionDao().observeAll()
    val pendingMeasurementCount: Flow<Int> = database.measurementDao().observePendingCount()
    val pendingSyncCount: Flow<Int> = database.syncQueueDao().observeCount()

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
                clientId = "demo-seed-${draft.pointCode}",
                measuredAt = time,
                queueForSync = false
            )
        }
    }

    /** Guarda medición y alerta en una transacción; la cola offline se escribe en la misma transacción. */
    suspend fun saveMeasurement(
        farm: String,
        shed: String,
        pointCode: String,
        location: String,
        chlorinePpm: Double,
        createdBy: String,
        clientId: String = UUID.randomUUID().toString(),
        measuredAt: Long = System.currentTimeMillis(),
        queueForSync: Boolean = true
    ): Long {
        val status = ChlorineRules.status(chlorinePpm)
        val measurementId = database.withTransaction {
            val id = database.measurementDao().insert(
                MeasurementEntity(
                    clientId = clientId,
                    farm = farm.trim(),
                    shed = shed.trim(),
                    pointCode = pointCode.trim(),
                    location = location.trim(),
                    chlorinePpm = chlorinePpm,
                    measuredAt = measuredAt,
                    status = status.name,
                    createdBy = createdBy,
                    synced = !queueForSync
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

            if (queueForSync) {
                database.syncQueueDao().insert(
                    SyncQueueEntity(
                        eventType = EVENT_MEASUREMENT,
                        eventClientId = clientId,
                        payloadJson = measurementPayload(
                            clientId, farm, shed, pointCode, location,
                            chlorinePpm, measuredAt, status.name, createdBy
                        ).toString(),
                        createdAt = measuredAt
                    )
                )
            }
            id
        }

        // WorkManager is scheduled only after the local transaction is committed.
        if (queueForSync) SyncScheduler.enqueue(appContext)
        return measurementId
    }

    /** Registra la acción y la nueva medición de verificación de forma local y atómica. */
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

        val saved = database.withTransaction {
            val alert = database.alertDao().findById(alertId)
                ?: return@withTransaction false
            val originalMeasurement = database.measurementDao().findById(alert.measurementId)
                ?: return@withTransaction false

            val createdAt = System.currentTimeMillis()
            val verificationClientId = UUID.randomUUID().toString()
            val verificationStatus = ChlorineRules.status(verifiedPpm)
            val verificationMeasurementId = database.measurementDao().insert(
                MeasurementEntity(
                    clientId = verificationClientId,
                    farm = originalMeasurement.farm,
                    shed = originalMeasurement.shed,
                    pointCode = originalMeasurement.pointCode,
                    location = originalMeasurement.location,
                    chlorinePpm = verifiedPpm,
                    measuredAt = createdAt,
                    status = verificationStatus.name,
                    createdBy = responsible,
                    synced = false
                )
            )

            // La medición de verificación también forma parte del historial.
            if (verificationStatus != ChlorineStatus.NORMAL) {
                database.alertDao().insert(
                    AlertEntity(
                        measurementId = verificationMeasurementId,
                        farm = originalMeasurement.farm,
                        pointCode = originalMeasurement.pointCode,
                        chlorinePpm = verifiedPpm,
                        issue = ChlorineRules.issue(verifiedPpm),
                        severity = ChlorineRules.severity(verifiedPpm),
                        createdAt = createdAt
                    )
                )
            }

            database.syncQueueDao().insert(
                SyncQueueEntity(
                    eventType = EVENT_MEASUREMENT,
                    eventClientId = verificationClientId,
                    payloadJson = measurementPayload(
                        clientId = verificationClientId,
                        farm = originalMeasurement.farm,
                        shed = originalMeasurement.shed,
                        pointCode = originalMeasurement.pointCode,
                        location = originalMeasurement.location,
                        chlorinePpm = verifiedPpm,
                        measuredAt = createdAt,
                        status = verificationStatus.name,
                        createdBy = responsible
                    ).toString(),
                    createdAt = createdAt
                )
            )

            val actionClientId = UUID.randomUUID().toString()
            val action = CorrectiveActionEntity(
                clientId = actionClientId,
                alertId = alertId,
                description = description.trim(),
                responsible = responsible,
                createdAt = createdAt,
                verifiedPpm = verifiedPpm,
                evidenceSource = evidenceSource,
                evidenceUri = evidenceUri,
                status = "PENDIENTE_VALIDACION",
                validatedBy = null,
                synced = false
            )
            database.correctiveActionDao().insert(action)
            database.syncQueueDao().insert(
                SyncQueueEntity(
                    eventType = EVENT_ACTION,
                    eventClientId = actionClientId,
                    payloadJson = actionPayload(
                        clientId = actionClientId,
                        measurementClientId = originalMeasurement.clientId,
                        verificationMeasurementClientId = verificationClientId,
                        description = action.description,
                        responsible = responsible,
                        createdAt = createdAt,
                        verifiedPpm = verifiedPpm,
                        evidenceSource = evidenceSource,
                        evidenceAvailable = !evidenceUri.isNullOrBlank(),
                        status = action.status,
                        validatedBy = null
                    ).toString(),
                    createdAt = createdAt
                )
            )
            true
        }
        if (saved) SyncScheduler.enqueue(appContext)
        return saved
    }

    suspend fun validateLatestAction(
        alertId: Long,
        supervisorEmail: String,
        role: UserRole
    ): ValidationResult {
        if (role != UserRole.SUPERVISOR) {
            return ValidationResult(false, "Solo un Supervisor puede validar y cerrar alertas.")
        }
        val result = database.withTransaction {
            val alert = database.alertDao().findById(alertId)
                ?: return@withTransaction ValidationResult(false, "No se encontró la alerta.")
            if (alert.status != "ABIERTA") {
                return@withTransaction ValidationResult(false, "La alerta ya está cerrada.")
            }
            val action = database.correctiveActionDao().latestForAlert(alertId)
                ?: return@withTransaction ValidationResult(false, "Primero registra una acción correctiva.")
            if (action.status != "PENDIENTE_VALIDACION") {
                return@withTransaction ValidationResult(false, "La última acción ya fue validada o no está pendiente.")
            }
            if (!ChlorineRules.canCloseAlert(action.verifiedPpm)) {
                return@withTransaction ValidationResult(
                    false,
                    "La verificación debe quedar entre 3,00 y 5,00 ppm para cerrar la alerta."
                )
            }
            val updated = action.copy(status = "VALIDADA", validatedBy = supervisorEmail, synced = false)
            database.correctiveActionDao().update(updated)
            database.alertDao().update(alert.copy(status = "CERRADA"))

            val measurement = database.measurementDao().findById(alert.measurementId)
                ?: return@withTransaction ValidationResult(false, "No se encontró la medición asociada.")
            database.syncQueueDao().insert(
                SyncQueueEntity(
                    eventType = EVENT_ACTION_UPDATE,
                    eventClientId = updated.clientId,
                    payloadJson = actionPayload(
                        clientId = updated.clientId,
                        measurementClientId = measurement.clientId,
                        verificationMeasurementClientId = null,
                        description = updated.description,
                        responsible = updated.responsible,
                        createdAt = updated.createdAt,
                        verifiedPpm = updated.verifiedPpm,
                        evidenceSource = updated.evidenceSource,
                        evidenceAvailable = !updated.evidenceUri.isNullOrBlank(),
                        status = updated.status,
                        validatedBy = updated.validatedBy
                    ).toString(),
                    createdAt = System.currentTimeMillis()
                )
            )
            ValidationResult(true, "Acción validada y alerta cerrada.")
        }
        if (result.success) SyncScheduler.enqueue(appContext)
        return result
    }

    private fun measurementPayload(
        clientId: String,
        farm: String,
        shed: String,
        pointCode: String,
        location: String,
        chlorinePpm: Double,
        measuredAt: Long,
        status: String,
        createdBy: String
    ) = JSONObject()
        .put("clientId", clientId)
        .put("farm", farm.trim())
        .put("shed", shed.trim())
        .put("pointCode", pointCode.trim())
        .put("location", location.trim())
        .put("chlorinePpm", chlorinePpm)
        .put("measuredAt", measuredAt)
        .put("status", status)
        .put("createdBy", createdBy)

    private fun actionPayload(
        clientId: String,
        measurementClientId: String,
        verificationMeasurementClientId: String? = null,
        description: String,
        responsible: String,
        createdAt: Long,
        verifiedPpm: Double,
        evidenceSource: String,
        evidenceAvailable: Boolean,
        status: String,
        validatedBy: String?
    ) = JSONObject()
        .put("clientId", clientId)
        .put("measurementClientId", measurementClientId)
        .put("verificationMeasurementClientId", verificationMeasurementClientId ?: JSONObject.NULL)
        .put("description", description)
        .put("responsible", responsible)
        .put("createdAt", createdAt)
        .put("verifiedPpm", verifiedPpm)
        .put("evidenceSource", evidenceSource)
        .put("evidenceAvailable", evidenceAvailable)
        .put("status", status)
        .put("validatedBy", validatedBy ?: JSONObject.NULL)

    companion object {
        const val EVENT_MEASUREMENT = "MEASUREMENT"
        const val EVENT_ACTION = "ACTION"
        const val EVENT_ACTION_UPDATE = "ACTION_UPDATE"
    }
}

data class ValidationResult(val success: Boolean, val message: String)
