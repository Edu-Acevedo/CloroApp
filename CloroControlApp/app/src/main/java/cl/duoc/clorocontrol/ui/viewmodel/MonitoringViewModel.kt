package cl.duoc.clorocontrol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.clorocontrol.data.local.AlertEntity
import cl.duoc.clorocontrol.data.local.CorrectiveActionEntity
import cl.duoc.clorocontrol.data.local.MeasurementEntity
import cl.duoc.clorocontrol.data.repository.ChlorineRepository
import cl.duoc.clorocontrol.data.repository.ValidationResult
import cl.duoc.clorocontrol.model.UserRole
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MonitoringViewModel(private val repository: ChlorineRepository) : ViewModel() {
    val measurements: StateFlow<List<MeasurementEntity>> = repository.measurements.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val alerts: StateFlow<List<AlertEntity>> = repository.alerts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val actions: StateFlow<List<CorrectiveActionEntity>> = repository.actions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    init {
        viewModelScope.launch { repository.ensureSeedData() }
    }

    fun validateLatestAction(alertId: Long, supervisorEmail: String, role: UserRole, onResult: (ValidationResult) -> Unit) {
        viewModelScope.launch {
            onResult(repository.validateLatestAction(alertId, supervisorEmail, role))
        }
    }
}
