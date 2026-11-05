package cl.duoc.clorocontrol.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.clorocontrol.CloroControlApplication
import cl.duoc.clorocontrol.model.UserRole
import cl.duoc.clorocontrol.model.UserSession
import cl.duoc.clorocontrol.ui.viewmodel.MonitoringViewModel
import cl.duoc.clorocontrol.ui.viewmodel.viewModelFactory

@Composable
fun MeasurementsScreen(
    session: UserSession,
    app: CloroControlApplication,
    onRegisterMeasurement: () -> Unit
) {
    val vm: MonitoringViewModel = viewModel(
        factory = viewModelFactory { MonitoringViewModel(app.repository, app.connectivityObserver, app) }
    )
    val allMeasurements by vm.measurements.collectAsStateWithLifecycle()
    val measurements = allMeasurements.filter { session.canAccessFarm(it.farm) }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Mediciones", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Registros guardados localmente en el dispositivo. Los pendientes se sincronizan cuando hay conexión.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (session.role != UserRole.JEFATURA) {
            Button(onClick = onRegisterMeasurement, modifier = Modifier.fillMaxWidth()) {
                androidx.compose.material3.Icon(Icons.Default.Add, contentDescription = null)
                Text("  Nueva medición")
            }
        }
        if (measurements.isEmpty()) {
            Text("No hay mediciones para mostrar en tus granjas asignadas.", modifier = Modifier.padding(12.dp))
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(measurements, key = { it.id }) { item ->
                    MeasurementCard(item, Modifier.fillMaxWidth())
                }
                item { Spacer(Modifier.padding(bottom = 8.dp)) }
            }
        }
    }
}
