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

private val farms = listOf("Granja Norte", "Granja Central", "Granja Sur")
private fun validPointsFor(farm: String): List<String> = when (farm) {
    "Granja Norte" -> listOf("N-01", "N-02", "N-03", "N-04")
    "Granja Central" -> listOf("C-01", "C-02", "C-03", "C-04")
    else -> listOf("S-01", "S-02", "S-03", "S-04")
}

data class MeasurementFormUiState(
    val farm: String = "Granja Norte",
    val shed: String = "Galpón 1",
    val pointCode: String = "N-01",
    val location: String = "",
    val chlorineValue: String = "",
    val farmError: String? = null,
    val shedError: String? = null,
    val pointError: String? = null,
    val locationError: String? = null,
    val chlorineError: String? = null,
    val formMessage: String? = null,
    val saved: Boolean = false
)

class MeasurementFormViewModel(
    private val repository: ChlorineRepository,
    private val session: UserSession
) : ViewModel() {
    private val _uiState = MutableStateFlow(MeasurementFormUiState())
    val uiState: StateFlow<MeasurementFormUiState> = _uiState.asStateFlow()

    fun onFarmChange(value: String) {
        val prefix = when (value) {
            "Granja Norte" -> "N"
            "Granja Central" -> "C"
            else -> "S"
        }
        _uiState.update {
            it.copy(farm = value, pointCode = "$prefix-01", farmError = null, pointError = null)
        }
    }

    fun onShedChange(value: String) = _uiState.update { it.copy(shed = value, shedError = null) }
    fun onPointChange(value: String) = _uiState.update { it.copy(pointCode = value, pointError = null) }
    fun onLocationChange(value: String) = _uiState.update { it.copy(location = value, locationError = null) }
    fun onChlorineChange(value: String) = _uiState.update { it.copy(chlorineValue = value, chlorineError = null, formMessage = null) }

    fun save() {
        if (session.role == cl.duoc.clorocontrol.model.UserRole.JEFATURA) {
            _uiState.update { it.copy(formMessage = "El perfil Jefatura tiene permisos de consulta, no de registro.", saved = false) }
            return
        }
        val state = _uiState.value
        val value = state.chlorineValue.trim().replace(',', '.').toDoubleOrNull()
        val farmError = if (state.farm in farms) null else "Selecciona una granja."
        val shedError = if (state.shed.isBlank()) "Ingresa el galpón." else null
        val pointError = if (state.pointCode !in validPointsFor(state.farm)) "Selecciona un punto válido para la granja." else null
        val locationError = if (state.location.isBlank()) "Ingresa la ubicación del punto." else null
        val chlorineError = when {
            state.chlorineValue.isBlank() -> "Ingresa el nivel de cloro libre."
            value == null -> "Usa un número, por ejemplo 3,50."
            !value.isFinite() || value < 0.0 || value > 10.0 -> "El valor debe estar entre 0,00 y 10,00 ppm."
            else -> null
        }
        if (farmError != null || shedError != null || pointError != null || locationError != null || chlorineError != null) {
            _uiState.update {
                it.copy(
                    farmError = farmError, shedError = shedError, pointError = pointError,
                    locationError = locationError, chlorineError = chlorineError,
                    formMessage = null, saved = false
                )
            }
            return
        }
        val chlorine = requireNotNull(value)
        viewModelScope.launch {
            repository.saveMeasurement(
                farm = state.farm,
                shed = state.shed,
                pointCode = state.pointCode,
                location = state.location,
                chlorinePpm = chlorine,
                createdBy = session.email
            )
            val status = ChlorineRules.status(chlorine).label
            _uiState.update { it.copy(saved = true, formMessage = "Medición guardada. Estado: $status.") }
        }
    }
}
