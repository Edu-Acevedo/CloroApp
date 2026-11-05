package cl.duoc.clorocontrol.data.sync

import android.content.Context
import androidx.room.withTransaction
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cl.duoc.clorocontrol.data.local.AlertEntity
import cl.duoc.clorocontrol.data.local.CloroControlDatabase
import cl.duoc.clorocontrol.data.local.CorrectiveActionEntity
import cl.duoc.clorocontrol.data.local.MeasurementEntity
import cl.duoc.clorocontrol.data.repository.ChlorineRepository
import cl.duoc.clorocontrol.domain.ChlorineRules
import cl.duoc.clorocontrol.domain.ChlorineStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException

class MeasurementSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val database = CloroControlDatabase.getInstance(applicationContext)
        val queueDao = database.syncQueueDao()
        val api = CloroControlHttpApi()

        try {
            // 1. Enviar primero los eventos locales pendientes, respetando el orden de creación.
            for (event in queueDao.pending()) {
                try {
                    val payload = JSONObject(event.payloadJson)
                    when (event.eventType) {
                        ChlorineRepository.EVENT_MEASUREMENT -> {
                            val response = api.postMeasurement(payload)
                            if (response.first !in 200..299) throw SyncHttpException(response.first, response.second)
                            database.measurementDao().markSyncedByClientId(event.eventClientId)
                        }
                        ChlorineRepository.EVENT_ACTION -> {
                            val response = api.postAction(payload)
                            if (response.first !in 200..299) throw SyncHttpException(response.first, response.second)
                            database.correctiveActionDao().markSyncedByClientId(event.eventClientId)
                        }
                        ChlorineRepository.EVENT_ACTION_UPDATE -> {
                            val response = api.updateAction(event.eventClientId, payload)
                            if (response.first !in 200..299) throw SyncHttpException(response.first, response.second)
                            database.correctiveActionDao().markSyncedByClientId(event.eventClientId)
                        }
                        else -> throw IOException("Tipo de evento de sincronización desconocido: ${event.eventType}")
                    }
                    queueDao.delete(event.id)
                } catch (error: SyncHttpException) {
                    queueDao.recordFailure(event.id, error.message ?: "Error HTTP")
                    return@withContext if (error.statusCode >= 500 || error.statusCode == 408 || error.statusCode == 429) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                } catch (error: IOException) {
                    queueDao.recordFailure(event.id, error.message ?: "Error de conexión")
                    return@withContext Result.retry()
                }
            }

            // 2. Descargar cambios compartidos y fusionarlos en Room (la UI solo lee desde Room).
            pullMeasurements(api, database)
            pullActions(api, database)
            Result.success()
        } catch (error: SyncHttpException) {
            Result.retry()
        } catch (error: IOException) {
            Result.retry()
        } catch (error: Exception) {
            Result.retry()
        }
    }

    private suspend fun pullMeasurements(api: CloroControlHttpApi, database: CloroControlDatabase) {
        val rows = api.fetchMeasurements()
        for (index in 0 until rows.length()) {
            val item = rows.getJSONObject(index)
            val clientId = item.getString("clientId")
            val existing = database.measurementDao().findByClientId(clientId)
            if (existing != null) {
                database.measurementDao().markSyncedByClientId(clientId)
                continue
            }

            val status = item.optString("status", ChlorineRules.status(item.getDouble("chlorinePpm")).name)
            val measurement = MeasurementEntity(
                clientId = clientId,
                farm = item.optString("farm"),
                shed = item.optString("shed"),
                pointCode = item.optString("pointCode"),
                location = item.optString("location"),
                chlorinePpm = item.optDouble("chlorinePpm"),
                measuredAt = item.optLong("measuredAt"),
                status = status,
                createdBy = item.optString("createdBy", "usuario.remoto"),
                synced = true
            )

            database.withTransaction {
                val localId = database.measurementDao().insertRemote(measurement)
                if (localId > 0 && status != ChlorineStatus.NORMAL.name) {
                    database.alertDao().insert(
                        AlertEntity(
                            measurementId = localId,
                            farm = measurement.farm,
                            pointCode = measurement.pointCode,
                            chlorinePpm = measurement.chlorinePpm,
                            issue = ChlorineRules.issue(measurement.chlorinePpm),
                            severity = ChlorineRules.severity(measurement.chlorinePpm),
                            createdAt = measurement.measuredAt
                        )
                    )
                }
            }
        }
    }

    private suspend fun pullActions(api: CloroControlHttpApi, database: CloroControlDatabase) {
        val rows = api.fetchActions()
        for (index in 0 until rows.length()) {
            val item = rows.getJSONObject(index)
            val clientId = item.getString("clientId")
            val measurementClientId = item.optString("measurementClientId")
            val alertId = database.alertDao().findAlertIdByMeasurementClientId(measurementClientId) ?: continue
            val existing = database.correctiveActionDao().findByClientId(clientId)
            val action = CorrectiveActionEntity(
                id = existing?.id ?: 0L,
                clientId = clientId,
                alertId = alertId,
                description = item.optString("description"),
                responsible = item.optString("responsible"),
                createdAt = item.optLong("createdAt"),
                verifiedPpm = item.optDouble("verifiedPpm"),
                evidenceSource = item.optString("evidenceSource", "Sin evidencia"),
                // La ruta de evidencia de otro dispositivo no es accesible localmente.
                evidenceUri = existing?.evidenceUri,
                status = item.optString("status", "PENDIENTE_VALIDACION"),
                validatedBy = item.optString("validatedBy").takeIf { it.isNotBlank() && it != "null" },
                synced = true
            )
            database.withTransaction {
                if (existing == null) {
                    database.correctiveActionDao().insertRemote(action)
                } else {
                    // Refleja en Room las validaciones realizadas en otro dispositivo.
                    database.correctiveActionDao().update(action)
                }
                if (action.status == "VALIDADA") {
                    database.alertDao().findById(alertId)?.let { alert ->
                        database.alertDao().update(alert.copy(status = "CERRADA"))
                    }
                }
            }
        }
    }
}
