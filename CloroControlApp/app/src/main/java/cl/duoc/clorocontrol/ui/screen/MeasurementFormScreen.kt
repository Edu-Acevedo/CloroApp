package cl.duoc.clorocontrol.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.clorocontrol.CloroControlApplication
import cl.duoc.clorocontrol.model.UserSession
import cl.duoc.clorocontrol.ui.viewmodel.MeasurementFormViewModel
import cl.duoc.clorocontrol.ui.viewmodel.viewModelFactory

@Composable
fun MeasurementFormScreen(
    session: UserSession,
    app: CloroControlApplication,
    onSaved: () -> Unit
) {
    val vm: MeasurementFormViewModel = viewModel(
        key = "measurement-form-${session.email}",
        factory = viewModelFactory { MeasurementFormViewModel(app.repository, session) }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }
    val points = when (state.farm) {
        "Granja Norte" -> listOf("N-01", "N-02", "N-03", "N-04")
        "Granja Central" -> listOf("C-01", "C-02", "C-03", "C-04")
        else -> listOf("S-01", "S-02", "S-03", "S-04")
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Registrar medición", style = MaterialTheme.typography.headlineMedium)
        Text("Ingresa el resultado de cloro libre en ppm. Los datos quedan guardados en SQLite.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        DropdownField("Granja", state.farm, listOf("Granja Norte", "Granja Central", "Granja Sur"), vm::onFarmChange, state.farmError)
        OutlinedTextField(
            value = state.shed, onValueChange = vm::onShedChange, modifier = Modifier.fillMaxWidth(),
            label = { Text("Galpón") }, singleLine = true, isError = state.shedError != null,
            supportingText = { state.shedError?.let { Text(it) } },
            trailingIcon = { if (state.shedError != null) androidx.compose.material3.Icon(Icons.Default.ErrorOutline, contentDescription = "Error") }
        )
        DropdownField("Punto de medición", state.pointCode, points, vm::onPointChange, state.pointError)
        OutlinedTextField(
            value = state.location, onValueChange = vm::onLocationChange, modifier = Modifier.fillMaxWidth(),
            label = { Text("Ubicación del punto") }, placeholder = { Text("Ej. Extremo de línea") }, singleLine = true,
            isError = state.locationError != null, supportingText = { state.locationError?.let { Text(it) } },
            trailingIcon = { if (state.locationError != null) androidx.compose.material3.Icon(Icons.Default.ErrorOutline, contentDescription = "Error") }
        )
        OutlinedTextField(
            value = state.chlorineValue, onValueChange = vm::onChlorineChange, modifier = Modifier.fillMaxWidth(),
            label = { Text("Cloro libre (ppm)") }, placeholder = { Text("3,50") }, singleLine = true,
            isError = state.chlorineError != null, supportingText = {
                Text(state.chlorineError ?: "Acepta valores desde 0,00 hasta 10,00 ppm; se mostrarán dos decimales.")
            }, trailingIcon = {
                if (state.chlorineError != null) androidx.compose.material3.Icon(Icons.Default.ErrorOutline, contentDescription = "Error de cloro")
            }
        )
        state.formMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium) }
        Button(onClick = vm::save, modifier = Modifier.fillMaxWidth()) { Text("Guardar medición") }
        Text("Responsable: ${session.email}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.padding(bottom = 16.dp))
    }
}

@Composable
private fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    error: String?
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            readOnly = true,
            label = { Text(label) },
            singleLine = true,
            isError = error != null,
            supportingText = { error?.let { Text(it) } },
            trailingIcon = {
                if (error != null) androidx.compose.material3.Icon(Icons.Default.ErrorOutline, contentDescription = "Error")
                else androidx.compose.material3.Icon(Icons.Default.ExpandMore, contentDescription = "Mostrar opciones")
            }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
