package cl.duoc.clorocontrol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.clorocontrol.data.repository.ChlorineRepository
import cl.duoc.clorocontrol.domain.FormValidators
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
    val saved: Boolean = false,
    val isSaving: Boolean = false
)

class ActionFormViewModel(
    private val repository: ChlorineRepository,
    private val session: UserSession,
    private val alertId: Long
) : ViewModel() {
    private val _uiState = MutableStateFlow(ActionFormUiState())
    val uiState: StateFlow<ActionFormUiState> = _uiState.asStateFlow()

    fun onDescriptionChange(value: String) = _uiState.update {
        it.copy(description = value, descriptionError = null, saved = false)
    }

    fun onVerifiedChange(value: String) = _uiState.update {
        it.copy(verifiedValue = value, verifiedError = null, formMessage = null, saved = false)
    }

    fun onEvidenceChange(source: String, uri: String? = null) = _uiState.update {
        it.copy(evidenceSource = source, evidenceUri = uri)
    }

    fun save() {
        if (!session.canRegisterActions) {
            _uiState.update { it.copy(formMessage = "El perfil Jefatura tiene permisos de consulta, no de registro.", saved = false) }
            return
        }
        val state = _uiState.value
        val descriptionError = FormValidators.validateDescription(state.description)
        val verifiedError = FormValidators.validatePpm(state.verifiedValue)
        if (descriptionError != null || verifiedError != null) {
            _uiState.update {
                it.copy(descriptionError = descriptionError, verifiedError = verifiedError, formMessage = null, saved = false)
            }
            return
        }

        val ppm = requireNotNull(FormValidators.parsePpm(state.verifiedValue))
        _uiState.update { it.copy(isSaving = true, formMessage = null, saved = false) }
        viewModelScope.launch {
            runCatching {
                repository.registerCorrectiveAction(
                    alertId = alertId,
                    description = state.description,
                    responsible = session.email,
                    verifiedPpm = ppm,
                    evidenceSource = state.evidenceSource,
                    evidenceUri = state.evidenceUri,
                    role = session.role
                )
            }.onSuccess { saved ->
                val message = if (saved) {
                    "Acción guardada localmente. Queda pendiente de validación del supervisor y sincronización."
                } else {
                    "No se encontró la alerta asociada."
                }
                _uiState.update { it.copy(isSaving = false, saved = saved, formMessage = message) }
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false, saved = false, formMessage = error.message ?: "No se pudo guardar la acción.") }
            }
        }
    }
}
