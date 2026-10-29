package cl.duoc.clorocontrol.ui.screen

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import cl.duoc.clorocontrol.CloroControlApplication
import cl.duoc.clorocontrol.model.UserSession
import cl.duoc.clorocontrol.ui.viewmodel.ActionFormViewModel
import cl.duoc.clorocontrol.ui.viewmodel.viewModelFactory

@Composable
fun ActionFormScreen(
    alertId: Long,
    session: UserSession,
    app: CloroControlApplication,
    onSaved: () -> Unit
) {
    val vm: ActionFormViewModel = viewModel(
        key = "action-form-$alertId-${session.email}",
        factory = viewModelFactory { ActionFormViewModel(app.repository, session, alertId) }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()
    var cameraBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            cameraBitmap = bitmap
            vm.onEvidenceChange("Cámara")
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            cameraBitmap = null
            vm.onEvidenceChange("Galería", uri.toString())
        }
    }
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Acción correctiva", style = MaterialTheme.typography.headlineMedium)
        Text("Alerta #$alertId · responsable ${session.email}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = state.description,
            onValueChange = vm::onDescriptionChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Acción realizada") },
            placeholder = { Text("Ej. Revisar dosificador y repetir medición") },
            minLines = 3,
            isError = state.descriptionError != null,
            supportingText = { state.descriptionError?.let { Text(it) } },
            trailingIcon = { if (state.descriptionError != null) androidx.compose.material3.Icon(Icons.Default.ErrorOutline, contentDescription = "Error de descripción") }
        )
        OutlinedTextField(
            value = state.verifiedValue,
            onValueChange = vm::onVerifiedChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Cloro de verificación (ppm)") },
            placeholder = { Text("3,50") },
            singleLine = true,
            isError = state.verifiedError != null,
            supportingText = { Text(state.verifiedError ?: "Para cerrar la alerta, la verificación debe quedar entre 3,00 y 5,00 ppm.") },
            trailingIcon = { if (state.verifiedError != null) androidx.compose.material3.Icon(Icons.Default.ErrorOutline, contentDescription = "Error de medición") }
        )
        Text("Evidencia opcional", style = MaterialTheme.typography.titleMedium)
        androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { cameraLauncher.launch(null) }, modifier = Modifier.weight(1f)) {
                androidx.compose.material3.Icon(Icons.Default.CameraAlt, contentDescription = null)
                Text(" Cámara")
            }
            OutlinedButton(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.weight(1f)) {
                androidx.compose.material3.Icon(Icons.Default.ImageSearch, contentDescription = null)
                Text(" Galería")
            }
        }
        when {
            cameraBitmap != null -> {
                Image(
                    bitmap = cameraBitmap!!.asImageBitmap(),
                    contentDescription = "Fotografía tomada desde la cámara",
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    contentScale = ContentScale.Crop
                )
                Text("Evidencia obtenida desde la cámara.", style = MaterialTheme.typography.bodySmall)
            }
            state.evidenceUri != null -> {
                AsyncImage(
                    model = state.evidenceUri,
                    contentDescription = "Imagen seleccionada desde la galería",
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    contentScale = ContentScale.Crop
                )
                Text("Evidencia seleccionada desde la galería.", style = MaterialTheme.typography.bodySmall)
            }
            else -> Text("No se adjuntó evidencia.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (state.evidenceSource != "Sin evidencia") {
            Text("Origen: ${state.evidenceSource}", style = MaterialTheme.typography.labelMedium)
        }
        state.formMessage?.let { Text(it, color = if (state.saved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }
        Button(onClick = vm::save, modifier = Modifier.fillMaxWidth()) { Text("Guardar acción correctiva") }
        Spacer(Modifier.padding(bottom = 16.dp))
    }
}
