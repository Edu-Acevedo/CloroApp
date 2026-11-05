package cl.duoc.clorocontrol.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.clorocontrol.CloroControlApplication
import cl.duoc.clorocontrol.model.UserRole
import cl.duoc.clorocontrol.model.UserSession
import cl.duoc.clorocontrol.ui.theme.CriticalRed
import cl.duoc.clorocontrol.ui.theme.NormalGreen
import cl.duoc.clorocontrol.ui.viewmodel.MonitoringViewModel
import cl.duoc.clorocontrol.ui.viewmodel.viewModelFactory

@Composable
fun DashboardScreen(
    session: UserSession,
    app: CloroControlApplication,
    onOpenMeasurements: () -> Unit,
    onOpenAlerts: () -> Unit,
    onRegisterMeasurement: () -> Unit
) {
    val vm: MonitoringViewModel = viewModel(
        factory = viewModelFactory { MonitoringViewModel(app.repository, app.connectivityObserver, app) }
    )
    val allMeasurements by vm.measurements.collectAsStateWithLifecycle()
    val allAlerts by vm.alerts.collectAsStateWithLifecycle()
    val pendingCount by vm.pendingCount.collectAsStateWithLifecycle()
    val isOnline by vm.isOnline.collectAsStateWithLifecycle()
    val measurements = allMeasurements.filter { session.canAccessFarm(it.farm) }
    val alerts = allAlerts.filter { session.canAccessFarm(it.farm) }
    val openAlerts = alerts.filter { it.status == "ABIERTA" }
    val critical = openAlerts.count { it.severity == "CRÍTICA" }
    val recent = measurements.take(12).map { it.chlorinePpm }
    val average = recent.average().takeIf { it.isFinite() }
    val minimum = recent.minOrNull()
    val maximum = recent.maxOrNull()
    val farmAverages = measurements.groupBy { it.farm }.mapValues { (_, rows) ->
        rows.map { it.chlorinePpm }.average()
    }

    BoxWithConstraints {
        val wide = maxWidth >= 600.dp
        val horizontalPadding = if (wide) 28.dp else 16.dp

        LazyColumn(
            modifier = Modifier.padding(horizontal = horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(if (wide) 18.dp else 14.dp)
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(
                        "Hola, ${session.role.label.lowercase().replaceFirstChar { it.uppercase() }}",
                        style = if (wide) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium
                    )
                    Text(session.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "Granjas visibles: ${session.assignedFarms.joinToString()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Estado general del agua", style = MaterialTheme.typography.titleMedium)
                            Text("Rango objetivo del MVP: 3,00 a 5,00 ppm", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Lecturas de cloro libre simuladas; no son sensores reales.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOnline) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = null
                        )
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                if (isOnline) "Conexión disponible" else "Sin conexión de red",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                if (pendingCount > 0) "$pendingCount cambios pendientes de sincronización"
                                else "No hay cambios pendientes en la cola local",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (!isOnline) {
                                Text("Puedes seguir registrando y consultando datos guardados en el dispositivo.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (isOnline) {
                            OutlinedButton(onClick = vm::synchronizeNow) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Text(if (pendingCount > 0) "Sincronizar" else "Actualizar")
                            }
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(
                        title = "Mediciones",
                        value = measurements.size.toString(),
                        subtitle = "registros locales",
                        accent = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Alertas abiertas",
                        value = openAlerts.size.toString(),
                        subtitle = "$critical críticas",
                        accent = if (critical > 0) CriticalRed else NormalGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(
                        title = "Promedio",
                        value = average?.let { formatPpm(it) } ?: "—",
                        subtitle = "ppm · últimas 12",
                        accent = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Mínimo / Máximo",
                        value = if (minimum != null && maximum != null) "${formatPpm(minimum)} / ${formatPpm(maximum)}" else "—",
                        subtitle = "ppm · últimas 12",
                        accent = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                ChlorineTrendCard(values = measurements.take(12).reversed().map { it.chlorinePpm })
            }
            if (farmAverages.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Comparación por granja", style = MaterialTheme.typography.titleMedium)
                            farmAverages.toSortedMap().forEach { (farm, value) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(farm, style = MaterialTheme.typography.bodyMedium)
                                    Text("${formatPpm(value)} ppm", style = MaterialTheme.typography.titleSmall)
                                }
                            }
                        }
                    }
                }
            }
            item {
                AnimatedVisibility(visible = openAlerts.isNotEmpty()) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Hay desviaciones que requieren seguimiento", style = MaterialTheme.typography.titleMedium)
                            Text("Revisa las alertas abiertas, documenta las acciones correctivas y solicita validación.")
                            OutlinedButton(onClick = onOpenAlerts) { Text("Revisar alertas") }
                        }
                    }
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Últimas mediciones", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = onOpenMeasurements) { Text("Ver todas") }
                }
            }
            if (measurements.isEmpty()) {
                item { Text("Todavía no hay mediciones registradas.", modifier = Modifier.padding(12.dp)) }
            } else {
                items(measurements.take(4), key = { it.id }) { measurement ->
                    MeasurementCard(measurement, Modifier.fillMaxWidth())
                }
            }
            if (session.role != UserRole.JEFATURA) {
                item {
                    Button(onClick = onRegisterMeasurement, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null)
                        Text("  Registrar medición")
                    }
                }
            }
            item { Spacer(Modifier.padding(bottom = 12.dp)) }
        }
    }
}
