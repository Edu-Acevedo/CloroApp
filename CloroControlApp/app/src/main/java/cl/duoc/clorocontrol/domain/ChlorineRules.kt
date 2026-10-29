package cl.duoc.clorocontrol.domain

enum class ChlorineStatus(val label: String) {
    NORMAL("Normal"),
    ADVERTENCIA("Advertencia"),
    CRITICO("Crítico")
}

object ChlorineRules {
    /** Rangos académicos propuestos para el MVP; deben ser confirmados con el docente. */
    fun status(value: Double): ChlorineStatus = when {
        value < 2.0 || value > 6.0 -> ChlorineStatus.CRITICO
        value < 3.0 || value > 5.0 -> ChlorineStatus.ADVERTENCIA
        else -> ChlorineStatus.NORMAL
    }

    fun issue(value: Double): String =
        if (value < 3.0) "Cloro insuficiente" else "Cloro excesivo"

    fun severity(value: Double): String = when (status(value)) {
        ChlorineStatus.NORMAL -> "NORMAL"
        ChlorineStatus.ADVERTENCIA -> "ADVERTENCIA"
        ChlorineStatus.CRITICO -> "CRÍTICA"
    }

    fun canCloseAlert(verifiedPpm: Double): Boolean = status(verifiedPpm) == ChlorineStatus.NORMAL
}
