package cl.duoc.clorocontrol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.clorocontrol.data.repository.ChlorineRepository
import cl.duoc.clorocontrol.domain.ChlorineRules
import cl.duoc.clorocontrol.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActionFormUiState(
    val description: String = "",
    val verifiedValue: String = "",
    val evidenceSource: String = "Sin evidencia",
    val evidenceUri: String? = null,
    val descriptionError: String? = null,
    val verifiedError: String? = null,
    val formMessage: String? = null,
    val saved: Boolean = false
)

class ActionFormViewModel(
    private val repository: ChlorineRepository,
    private val session: UserSession,
    private val alertId: Long
) : ViewModel() {
    private val _uiState = MutableStateFlow(ActionFormUiState())
    val uiState: StateFlow<ActionFormUiState> = _uiState.asStateFlow()

    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value, descriptionError = null) }
    fun onVerifiedChange(value: String) = _uiState.update { it.copy(verifiedValue = value, verifiedError = null, formMessage = null) }
    fun onEvidenceChange(source: String, uri: String? = null) =
        _uiState.update { it.copy(evidenceSource = source, evidenceUri = uri) }

    fun save() {
        if (session.role == cl.duoc.clorocontrol.model.UserRole.JEFATURA) {
            _uiState.update { it.copy(formMessage = "El perfil Jefatura tiene permisos de consulta, no de registro.", saved = false) }
            return
        }
        val state = _uiState.value
        val verified = state.verifiedValue.trim().replace(',', '.').toDoubleOrNull()
        val descriptionError = when {
            state.description.isBlank() -> "Describe la acción realizada."
            state.description.trim().length < 8 -> "Escribe al menos 8 caracteres para describir la acción."
            else -> null
        }
        val verifiedError = when {
            state.verifiedValue.isBlank() -> "Ingresa la medición de verificación."
            verified == null -> "Usa un número, por ejemplo 3,50."
            !verified.isFinite() || verified < 0.0 || verified > 10.0 -> "El valor debe estar entre 0,00 y 10,00 ppm."
            else -> null
        }
        if (descriptionError != null || verifiedError != null) {
            _uiState.update {
                it.copy(descriptionError = descriptionError, verifiedError = verifiedError, formMessage = null, saved = false)
            }
            return
        }
        val ppm = requireNotNull(verified)
        viewModelScope.launch {
            val saved = repository.registerCorrectiveAction(
                alertId = alertId,
                description = state.description,
                responsible = session.email,
                verifiedPpm = ppm,
                evidenceSource = state.evidenceSource,
                evidenceUri = state.evidenceUri,
                role = session.role
            )
            val message = when {
                !saved -> "No se encontró la alerta asociada."
                session.role.name == "SUPERVISOR" && ChlorineRules.canCloseAlert(ppm) -> "Acción guardada y alerta cerrada por supervisión."
                else -> "Acción guardada. Queda pendiente de validación del supervisor."
            }
            _uiState.update { it.copy(saved = saved, formMessage = message) }
        }
    }
}
