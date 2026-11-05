package cl.duoc.clorocontrol.domain

object FormValidators {
    fun parsePpm(raw: String): Double? = raw.trim().replace(',', '.').toDoubleOrNull()

    fun validatePpm(raw: String): String? {
        val value = parsePpm(raw)
        return when {
            raw.isBlank() -> "Ingresa el nivel de cloro libre."
            value == null -> "Usa un número válido, por ejemplo 3,50."
            !value.isFinite() -> "Usa un número válido, por ejemplo 3,50."
            value < 0.0 -> "El valor debe ser un número mayor o igual a 0,00 ppm."
            !Regex("^[0-9]+([.,][0-9]{1,2})?$").matches(raw.trim()) -> "Registra el cloro con un máximo de dos decimales."
            else -> null
        }
    }

    fun validateDescription(raw: String): String? = when {
        raw.isBlank() -> "Describe la acción realizada."
        raw.trim().length < 8 -> "Escribe al menos 8 caracteres para describir la acción."
        raw.trim().length > 500 -> "La descripción no puede superar 500 caracteres."
        else -> null
    }

    fun validateEmail(raw: String): String? {
        val email = raw.trim()
        val pattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return when {
            email.isBlank() -> "Ingresa tu correo electrónico."
            !pattern.matches(email) -> "Ingresa un correo válido."
            else -> null
        }
    }
}
