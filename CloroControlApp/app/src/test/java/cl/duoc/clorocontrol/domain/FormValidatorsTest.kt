package cl.duoc.clorocontrol.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FormValidatorsTest {
    @Test
    fun `ppm accepts decimal comma and decimal point`() {
        assertEquals(3.5, FormValidators.parsePpm("3,50")!!, 0.0001)
        assertEquals(3.5, FormValidators.parsePpm("3.50")!!, 0.0001)
    }

    @Test
    fun `ppm validation accepts values inside allowed input range`() {
        assertNull(FormValidators.validatePpm("0,00"))
        assertNull(FormValidators.validatePpm("3,50"))
        assertNull(FormValidators.validatePpm("10,00"))
        // El caso define como crítico cualquier valor superior a 6 ppm y no fija un máximo superior.
        assertNull(FormValidators.validatePpm("12,50"))
        assertNull(FormValidators.validatePpm("3,5"))
    }

    @Test
    fun `ppm validation rejects empty nonnumeric and out of range values`() {
        assertEquals("Ingresa el nivel de cloro libre.", FormValidators.validatePpm(" "))
        assertEquals("Usa un número válido, por ejemplo 3,50.", FormValidators.validatePpm("abc"))
        assertEquals("El valor debe ser un número mayor o igual a 0,00 ppm.", FormValidators.validatePpm("-0,01"))
        assertEquals("Registra el cloro con un máximo de dos decimales.", FormValidators.validatePpm("3,555"))
    }

    @Test
    fun `description requires 8 to 500 characters`() {
        assertEquals("Describe la acción realizada.", FormValidators.validateDescription("  "))
        assertEquals("Escribe al menos 8 caracteres para describir la acción.", FormValidators.validateDescription("cambio"))
        assertNull(FormValidators.validateDescription("Se revisó el dosificador"))
    }

    @Test
    fun `email validates basic format`() {
        assertNull(FormValidators.validateEmail("operario@demo.cl"))
        assertEquals("Ingresa un correo válido.", FormValidators.validateEmail("operario-demo"))
    }
}
