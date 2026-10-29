package cl.duoc.clorocontrol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.clorocontrol.data.preferences.SessionStore
import cl.duoc.clorocontrol.model.UserRole
import cl.duoc.clorocontrol.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val EMAIL_PATTERN = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val formMessage: String? = null,
    val session: UserSession? = null,
    val hasLoadedSession: Boolean = false
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
        val password = current.password
        val emailError = when {
            email.isBlank() -> "Ingresa tu correo electrónico."
            !EMAIL_PATTERN.matches(email) -> "Ingresa un correo válido."
            else -> null
        }
        val passwordError = when {
            password.isBlank() -> "Ingresa tu contraseña."
            password.length < 6 -> "Debe contener al menos 6 caracteres."
            else -> null
        }
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError, formMessage = null) }
            return
        }

        val role = UserRole.fromDemoEmail(email)
        if (role == null || password != "Demo123") {
            _uiState.update {
                it.copy(
                    emailError = if (role == null) "Usa una cuenta de demostración." else null,
                    passwordError = if (password != "Demo123") "Contraseña de demostración incorrecta." else null,
                    formMessage = "Estas credenciales son ficticias y solo sirven para probar el MVP."
                )
            }
            return
        }

        viewModelScope.launch {
            sessionStore.saveSession(UserSession(email = email, role = role))
        }
    }

    fun signOut() {
        viewModelScope.launch { sessionStore.clearSession() }
    }
}
