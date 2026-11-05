package cl.duoc.clorocontrol.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.clorocontrol.CloroControlApplication
import cl.duoc.clorocontrol.data.local.CorrectiveActionEntity
import cl.duoc.clorocontrol.domain.ChlorineRules
import cl.duoc.clorocontrol.model.UserRole
import cl.duoc.clorocontrol.model.UserSession
import cl.duoc.clorocontrol.ui.viewmodel.MonitoringViewModel
import cl.duoc.clorocontrol.ui.viewmodel.viewModelFactory

@Composable
fun AlertsScreen(
    session: UserSession,
    app: CloroControlApplication,
    onRegisterAction: (Long) -> Unit
) {
    val vm: MonitoringViewModel = viewModel(
        factory = viewModelFactory { MonitoringViewModel(app.repository, app.connectivityObserver, app) }
    )
    val allAlerts by vm.alerts.collectAsStateWithLifecycle()
    val actions by vm.actions.collectAsStateWithLifecycle()
    var showClosed by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }
    val alerts = allAlerts.filter { session.canAccessFarm(it.farm) }
    val visibleAlerts = alerts.filter { showClosed || it.status == "ABIERTA" }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Alertas y desviaciones", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Las desviaciones se detectan automáticamente al guardar una medición fuera del rango propuesto.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        FilterChip(
            selected = showClosed,
            onClick = { showClosed = !showClosed },
            label = { Text(if (showClosed) "Abiertas y cerradas" else "Solo alertas abiertas") }
        )
        feedback?.let {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Text(it, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
        if (visibleAlerts.isEmpty()) {
            Text("No hay alertas para mostrar en este filtro.", modifier = Modifier.padding(12.dp))
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(visibleAlerts, key = { it.id }) { alert ->
                    val latest = actions.latestFor(alert.id)
                    val canRegister = session.canRegisterActions && alert.status == "ABIERTA"
                    val canValidate = session.role == UserRole.SUPERVISOR &&
                        alert.status == "ABIERTA" &&
                        latest != null &&
                        latest.status == "PENDIENTE_VALIDACION" &&
                        ChlorineRules.canCloseAlert(latest.verifiedPpm)

                    AlertCard(
                        alert = alert,
                        actionLabel = when {
                            canValidate -> "Validar y cerrar alerta"
                            canRegister && latest == null -> "Registrar acción correctiva"
                            canRegister -> "Registrar otra acción correctiva"
                            else -> null
                        },
                        onAction = when {
                            canValidate -> ({
                                vm.validateLatestAction(alert.id, session.email, session.role) { result ->
                                    feedback = result.message
                                }
                            })
                            canRegister -> ({ onRegisterAction(alert.id) })
                            else -> null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        extraContent = {
                            if (latest != null) {
                                Text("Última acción: ${latest.description}", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "Verificación: ${formatPpm(latest.verifiedPpm)} ppm · ${latest.status.replace('_', ' ')}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text("Responsable: ${latest.responsible}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                latest.validatedBy?.let { Text("Validada por: $it", style = MaterialTheme.typography.bodySmall) }
                            } else if (alert.status == "ABIERTA") {
                                Text("Sin acción correctiva registrada.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    )
                }
                item { Spacer(Modifier.padding(bottom = 10.dp)) }
            }
        }
    }
}

private fun List<CorrectiveActionEntity>.latestFor(alertId: Long): CorrectiveActionEntity? =
    filter { it.alertId == alertId }.maxByOrNull { it.createdAt }
