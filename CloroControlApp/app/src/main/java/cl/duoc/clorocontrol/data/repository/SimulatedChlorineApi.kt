package cl.duoc.clorocontrol.data.repository

/** Fuente local de datos ficticios que simula una API para la demostración académica. */
data class MeasurementDraft(
    val farm: String,
    val shed: String,
    val pointCode: String,
    val location: String,
    val chlorinePpm: Double,
    val ageMinutes: Long
)

object SimulatedChlorineApi {
    fun fetchInitialMeasurements(): List<MeasurementDraft> = listOf(
        MeasurementDraft("Granja Norte", "Galpón 1", "N-01", "Bebedero norte", 3.72, 4),
        MeasurementDraft("Granja Norte", "Galpón 1", "N-02", "Línea central", 2.65, 8),
        MeasurementDraft("Granja Norte", "Galpón 2", "N-03", "Bebedero sur", 4.21, 12),
        MeasurementDraft("Granja Norte", "Galpón 2", "N-04", "Extremo de línea", 1.72, 16),
        MeasurementDraft("Granja Central", "Galpón 1", "C-01", "Bebedero norte", 5.35, 20),
        MeasurementDraft("Granja Central", "Galpón 1", "C-02", "Línea central", 3.91, 24),
        MeasurementDraft("Granja Central", "Galpón 2", "C-03", "Bebedero sur", 4.08, 28),
        MeasurementDraft("Granja Central", "Galpón 2", "C-04", "Extremo de línea", 6.24, 32),
        MeasurementDraft("Granja Sur", "Galpón 1", "S-01", "Bebedero norte", 3.44, 36),
        MeasurementDraft("Granja Sur", "Galpón 1", "S-02", "Línea central", 2.12, 40),
        MeasurementDraft("Granja Sur", "Galpón 2", "S-03", "Bebedero sur", 4.66, 44),
        MeasurementDraft("Granja Sur", "Galpón 2", "S-04", "Extremo de línea", 5.00, 48)
    )
}
