package cl.duoc.clorocontrol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.clorocontrol.data.preferences.SessionStore
import cl.duoc.clorocontrol.domain.FormValidators
import cl.duoc.clorocontrol.model.UserRole
import cl.duoc.clorocontrol.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val formMessage: String? = null,
    val session: UserSession? = null,
    val hasLoadedSession: Boolean = false,
    val isLoading: Boolean = false
)

class AuthViewModel(private val sessionStore: SessionStore) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionStore.session.collect { session ->
                _uiState.update { it.copy(session = session, hasLoadedSession = true) }
            }
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, formMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, formMessage = null) }
    }

    fun signIn() {
        val current = _uiState.value
        val email = current.email.trim().lowercase()
        val emailError = FormValidators.validateEmail(email)
        val passwordError = when {
            current.password.isBlank() -> "Ingresa tu contraseña."
            current.password.length < 6 -> "Debe contener al menos 6 caracteres."
            else -> null
        }
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError, formMessage = null) }
            return
        }

        val role = UserRole.fromDemoEmail(email)
        if (role == null || current.password != "Demo123") {
            _uiState.update {
                it.copy(
                    emailError = if (role == null) "Usa una cuenta de demostración."
                    else null,
                    passwordError = if (current.password != "Demo123") "Contraseña de demostración incorrecta." else null,
                    formMessage = "Estas credenciales son ficticias y solo sirven para probar el MVP."
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, formMessage = null) }
        viewModelScope.launch {
            runCatching { sessionStore.saveSession(UserSession(email = email, role = role)) }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, formMessage = error.message ?: "No se pudo iniciar sesión.") }
                }
                .onSuccess { _uiState.update { it.copy(isLoading = false) } }
        }
    }

    fun signOut() {
        viewModelScope.launch { sessionStore.clearSession() }
    }
}
