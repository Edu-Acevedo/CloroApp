package cl.duoc.clorocontrol.model

enum class UserRole(val label: String) {
    OPERARIO("Operario"),
    SUPERVISOR("Supervisor"),
    JEFATURA("Jefatura");

    companion object {
        fun fromName(value: String?): UserRole? = entries.firstOrNull { it.name == value }

        fun fromDemoEmail(email: String): UserRole? = DemoAccounts.find(email)?.role
    }
}

data class DemoAccount(
    val email: String,
    val role: UserRole,
    val assignedFarms: Set<String>
)

/** 15 cuentas ficticias de referencia: cinco por cada rol del caso. */
object DemoAccounts {
    private val allFarms = setOf("Granja Norte", "Granja Central", "Granja Sur")

    val accounts: List<DemoAccount> = listOf(
        DemoAccount("operario@demo.cl", UserRole.OPERARIO, allFarms),
        DemoAccount("operario02@demo.cl", UserRole.OPERARIO, allFarms),
        DemoAccount("operario03@demo.cl", UserRole.OPERARIO, setOf("Granja Norte")),
        DemoAccount("operario04@demo.cl", UserRole.OPERARIO, setOf("Granja Sur")),
        DemoAccount("operario05@demo.cl", UserRole.OPERARIO, setOf("Granja Central")),

        DemoAccount("supervisor@demo.cl", UserRole.SUPERVISOR, allFarms),
        DemoAccount("supervisor02@demo.cl", UserRole.SUPERVISOR, setOf("Granja Central")),
        DemoAccount("supervisor03@demo.cl", UserRole.SUPERVISOR, setOf("Granja Sur")),
        DemoAccount("supervisor04@demo.cl", UserRole.SUPERVISOR, setOf("Granja Norte")),
        DemoAccount("supervisor05@demo.cl", UserRole.SUPERVISOR, setOf("Granja Central", "Granja Sur")),

        DemoAccount("jefatura@demo.cl", UserRole.JEFATURA, allFarms),
        DemoAccount("jefatura02@demo.cl", UserRole.JEFATURA, allFarms),
        DemoAccount("jefatura03@demo.cl", UserRole.JEFATURA, allFarms),
        DemoAccount("jefatura04@demo.cl", UserRole.JEFATURA, allFarms),
        DemoAccount("jefatura05@demo.cl", UserRole.JEFATURA, allFarms)
    )

    fun find(email: String): DemoAccount? =
        accounts.firstOrNull { it.email == email.trim().lowercase() }
}

data class UserSession(
    val email: String,
    val role: UserRole
) {
    /** Asignaciones ficticias por cuenta; si una sesión antigua no coincide se aplica un valor seguro por rol. */
    val assignedFarms: Set<String>
        get() = DemoAccounts.find(email)?.assignedFarms ?: when (role) {
            UserRole.OPERARIO, UserRole.SUPERVISOR -> setOf("Granja Norte")
            UserRole.JEFATURA -> setOf("Granja Norte", "Granja Central", "Granja Sur")
        }

    fun canAccessFarm(farm: String): Boolean = farm in assignedFarms
    val canRegisterMeasurements: Boolean get() = role != UserRole.JEFATURA
    val canRegisterActions: Boolean get() = role != UserRole.JEFATURA
    val canValidateActions: Boolean get() = role == UserRole.SUPERVISOR
}
