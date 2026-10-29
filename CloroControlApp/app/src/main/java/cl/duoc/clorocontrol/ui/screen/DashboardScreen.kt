package cl.duoc.clorocontrol.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Info
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
    val vm: MonitoringViewModel = viewModel(factory = viewModelFactory { MonitoringViewModel(app.repository) })
    val measurements by vm.measurements.collectAsStateWithLifecycle()
    val alerts by vm.alerts.collectAsStateWithLifecycle()
    val openAlerts = alerts.filter { it.status == "ABIERTA" }
    val critical = openAlerts.count { it.severity == "CRÍTICA" }
    val latestAverage = measurements.take(12).map { it.chlorinePpm }.average().takeIf { it.isFinite() }

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 10.dp)) {
                Text("Hola, ${session.role.label.lowercase().replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.headlineMedium)
                Text(session.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        Text("Datos ficticios de demostración; no son lecturas de sensores reales.", style = MaterialTheme.typography.bodySmall)
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
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Promedio reciente", style = MaterialTheme.typography.titleMedium)
                        Text(latestAverage?.let { "${formatPpm(it)} ppm" } ?: "Sin mediciones", style = MaterialTheme.typography.headlineSmall)
                    }
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item {
            AnimatedVisibility(visible = openAlerts.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Hay desviaciones que requieren seguimiento", style = MaterialTheme.typography.titleMedium)
                        Text("Revisa las alertas abiertas y registra la acción correctiva correspondiente.")
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
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text("Registrar medición")
                }
            }
        }
        item { Spacer(Modifier.padding(bottom = 12.dp)) }
    }
}
