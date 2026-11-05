package cl.duoc.clorocontrol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.clorocontrol.data.repository.ChlorineRepository
import cl.duoc.clorocontrol.domain.ChlorineRules
import cl.duoc.clorocontrol.domain.FormValidators
import cl.duoc.clorocontrol.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val allFarms = listOf("Granja Norte", "Granja Central", "Granja Sur")
fun pointsForFarm(farm: String): List<String> = when (farm) {
    "Granja Norte" -> listOf("N-01", "N-02", "N-03", "N-04")
    "Granja Central" -> listOf("C-01", "C-02", "C-03", "C-04")
    "Granja Sur" -> listOf("S-01", "S-02", "S-03", "S-04")
    else -> emptyList()
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
    val saved: Boolean = false,
    val isSaving: Boolean = false
)

class MeasurementFormViewModel(
    private val repository: ChlorineRepository,
    private val session: UserSession
) : ViewModel() {
    val availableFarms: List<String> = allFarms.filter { session.canAccessFarm(it) }

    private val _uiState = MutableStateFlow(
        MeasurementFormUiState(
            farm = availableFarms.firstOrNull() ?: "Granja Norte",
            pointCode = pointsForFarm(availableFarms.firstOrNull() ?: "Granja Norte").first()
        )
    )
    val uiState: StateFlow<MeasurementFormUiState> = _uiState.asStateFlow()

    fun onFarmChange(value: String) {
        if (value !in availableFarms) return
        _uiState.update {
            it.copy(
                farm = value,
                pointCode = pointsForFarm(value).first(),
                farmError = null,
                pointError = null,
                saved = false
            )
        }
    }

    fun onShedChange(value: String) = _uiState.update { it.copy(shed = value, shedError = null, saved = false) }
    fun onPointChange(value: String) = _uiState.update { it.copy(pointCode = value, pointError = null, saved = false) }
    fun onLocationChange(value: String) = _uiState.update { it.copy(location = value, locationError = null, saved = false) }
    fun onChlorineChange(value: String) = _uiState.update {
        it.copy(chlorineValue = value, chlorineError = null, formMessage = null, saved = false)
    }

    fun save() {
        if (!session.canRegisterMeasurements) {
            _uiState.update { it.copy(formMessage = "El perfil Jefatura tiene permisos de consulta, no de registro.", saved = false) }
            return
        }
        val state = _uiState.value
        val farmError = if (state.farm in availableFarms) null else "Selecciona una granja asignada."
        val shedError = when {
            state.shed.isBlank() -> "Ingresa el galpón."
            state.shed.trim().length > 40 -> "El galpón no puede superar 40 caracteres."
            else -> null
        }
        val pointError = if (state.pointCode in pointsForFarm(state.farm)) null else "Selecciona un punto válido para la granja."
        val locationError = when {
            state.location.isBlank() -> "Ingresa la ubicación del punto."
            state.location.trim().length > 80 -> "La ubicación no puede superar 80 caracteres."
            else -> null
        }
        val chlorineError = FormValidators.validatePpm(state.chlorineValue)

        if (farmError != null || shedError != null || pointError != null || locationError != null || chlorineError != null) {
            _uiState.update {
                it.copy(
                    farmError = farmError,
                    shedError = shedError,
                    pointError = pointError,
                    locationError = locationError,
                    chlorineError = chlorineError,
                    formMessage = null,
                    saved = false
                )
            }
            return
        }

        val chlorine = requireNotNull(FormValidators.parsePpm(state.chlorineValue))
        _uiState.update { it.copy(isSaving = true, formMessage = null, saved = false) }
        viewModelScope.launch {
            runCatching {
                repository.saveMeasurement(
                    farm = state.farm,
                    shed = state.shed,
                    pointCode = state.pointCode,
                    location = state.location,
                    chlorinePpm = chlorine,
                    createdBy = session.email
                )
            }.onSuccess {
                val status = ChlorineRules.status(chlorine).label
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saved = true,
                        formMessage = "Medición guardada localmente. Estado: $status. Se sincronizará cuando haya conexión."
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isSaving = false, saved = false, formMessage = error.message ?: "No se pudo guardar la medición.")
                }
            }
        }
    }
}
