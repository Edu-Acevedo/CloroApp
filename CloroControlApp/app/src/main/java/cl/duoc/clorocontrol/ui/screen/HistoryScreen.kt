package cl.duoc.clorocontrol.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import cl.duoc.clorocontrol.data.local.MeasurementEntity
import cl.duoc.clorocontrol.ui.viewmodel.MonitoringViewModel
import cl.duoc.clorocontrol.ui.viewmodel.viewModelFactory

@Composable
fun HistoryScreen(app: CloroControlApplication) {
    val vm: MonitoringViewModel = viewModel(factory = viewModelFactory { MonitoringViewModel(app.repository) })
    val measurements by vm.measurements.collectAsStateWithLifecycle()
    val actions by vm.actions.collectAsStateWithLifecycle()
    var period by remember { mutableStateOf("7 días") }
    val duration = when (period) {
        "24 horas" -> 24 * 60 * 60 * 1000L
        "30 días" -> 30 * 24 * 60 * 60 * 1000L
        else -> 7 * 24 * 60 * 60 * 1000L
    }
    val cutoff = System.currentTimeMillis() - duration
    val visibleMeasurements = measurements.filter { it.measuredAt >= cutoff }
    val visibleActions = actions.filter { it.createdAt >= cutoff }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Historial", style = MaterialTheme.typography.headlineMedium)
            Text("Consulta mediciones y acciones correctivas locales.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("24 horas", "7 días", "30 días").forEach { option ->
                FilterChip(selected = period == option, onClick = { period = option }, label = { Text(option) })
            }
        }
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text("Mediciones (${visibleMeasurements.size})", style = MaterialTheme.typography.titleLarge)
            }
            if (visibleMeasurements.isEmpty()) {
                item { Text("No hay mediciones para este período.", modifier = Modifier.padding(8.dp)) }
            } else {
                items(visibleMeasurements, key = { "m-${it.id}" }) { MeasurementCard(it, Modifier.fillMaxWidth()) }
            }
            item {
                Text("Acciones correctivas (${visibleActions.size})", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
            }
            if (visibleActions.isEmpty()) {
                item { Text("Todavía no hay acciones correctivas en este período.", modifier = Modifier.padding(8.dp)) }
            } else {
                items(visibleActions, key = { "a-${it.id}" }) { CorrectiveActionCard(it) }
            }
            item { Spacer(Modifier.padding(bottom = 12.dp)) }
        }
    }
}

@Composable
private fun CorrectiveActionCard(action: CorrectiveActionEntity) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Alerta #${action.alertId} · ${action.status.replace('_', ' ')}", style = MaterialTheme.typography.titleMedium)
            Text(action.description, style = MaterialTheme.typography.bodyMedium)
            Text("Verificación: ${formatPpm(action.verifiedPpm)} ppm · ${formatTimestamp(action.createdAt)}", style = MaterialTheme.typography.bodySmall)
            Text("Responsable: ${action.responsible}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (action.evidenceSource != "Sin evidencia") {
                Text("Evidencia: ${action.evidenceSource}", style = MaterialTheme.typography.labelMedium)
            }
            action.validatedBy?.let { Text("Validada por: $it", style = MaterialTheme.typography.bodySmall) }
        }
    }
}
