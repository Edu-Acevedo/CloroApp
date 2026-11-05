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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import cl.duoc.clorocontrol.ui.viewmodel.pointsForFarm
import cl.duoc.clorocontrol.ui.viewmodel.viewModelFactory
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType


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
    val points = pointsForFarm(state.farm)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Registrar medición", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Ingresa el resultado de cloro libre. El registro se guarda primero en SQLite, por lo que puedes trabajar sin Internet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        DropdownField("Granja", state.farm, vm.availableFarms, vm::onFarmChange, state.farmError)
        Text(
            text = "Granjas disponibles: ${vm.availableFarms.joinToString(separator = " | ")}",
            style = MaterialTheme.typography.bodySmall
        )
        OutlinedTextField(
            value = state.shed,
            onValueChange = vm::onShedChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Galpón") },
            singleLine = true,
            isError = state.shedError != null,
            supportingText = { Text(state.shedError ?: "Ej. Galpón 1") },
            trailingIcon = { if (state.shedError != null) androidx.compose.material3.Icon(Icons.Default.ErrorOutline, contentDescription = "Error en galpón") }
        )
        DropdownField("Punto de medición", state.pointCode, points, vm::onPointChange, state.pointError)
        OutlinedTextField(
            value = state.location,
            onValueChange = vm::onLocationChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Ubicación del punto") },
            placeholder = { Text("Ej. Extremo de línea") },
            singleLine = true,
            isError = state.locationError != null,
            supportingText = { Text(state.locationError ?: "Describe el lugar exacto donde se tomó la muestra.") },
            trailingIcon = { if (state.locationError != null) androidx.compose.material3.Icon(Icons.Default.ErrorOutline, contentDescription = "Error en ubicación") }
        )
        OutlinedTextField(
            value = state.chlorineValue,
            onValueChange = vm::onChlorineChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Cloro libre (ppm)") },
            placeholder = { Text("3,50") },
            singleLine = true,
            isError = state.chlorineError != null,
            supportingText = {
                Text(state.chlorineError ?: "Acepta valores desde 0,00 ppm; las lecturas sobre 6,00 ppm se registran como desviación crítica. Máximo dos decimales.")
            },
            trailingIcon = {
                if (state.chlorineError != null) androidx.compose.material3.Icon(Icons.Default.ErrorOutline, contentDescription = "Error de cloro")
            }
        )
        state.formMessage?.let {
            Text(
                it,
                color = if (state.saved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Button(
            onClick = vm::save,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving
        ) {
            if (state.isSaving) CircularProgressIndicator()
            else Text("Guardar medición")
        }
        Text("Responsable: ${session.email}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.padding(bottom = 16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    error: String?
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            readOnly = true,
            label = { Text(label) },
            singleLine = true,
            isError = error != null,
            supportingText = {
                error?.let { Text(it) }
            },
            trailingIcon = {
                if (error != null) {
                    androidx.compose.material3.Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = "Error"
                    )
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                }
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
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