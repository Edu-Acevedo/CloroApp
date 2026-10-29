package cl.duoc.clorocontrol.model

enum class UserRole(val label: String) {
    OPERARIO("Operario"),
    SUPERVISOR("Supervisor"),
    JEFATURA("Jefatura");

    companion object {
        fun fromName(value: String?): UserRole? = entries.firstOrNull { it.name == value }

        fun fromDemoEmail(email: String): UserRole? = when (email.lowercase()) {
            "operario@demo.cl" -> OPERARIO
            "supervisor@demo.cl" -> SUPERVISOR
            "jefatura@demo.cl" -> JEFATURA
            else -> null
        }
    }
}

data class UserSession(
    val email: String,
    val role: UserRole
)
