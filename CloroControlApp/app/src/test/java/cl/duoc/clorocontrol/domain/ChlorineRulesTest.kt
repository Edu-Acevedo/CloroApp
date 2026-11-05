package cl.duoc.clorocontrol.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChlorineRulesTest {
    @Test
    fun `normal range includes both limits`() {
        assertEquals(ChlorineStatus.NORMAL, ChlorineRules.status(3.00))
        assertEquals(ChlorineStatus.NORMAL, ChlorineRules.status(5.00))
    }

    @Test
    fun `warning range includes lower and upper thresholds`() {
        assertEquals(ChlorineStatus.ADVERTENCIA, ChlorineRules.status(2.00))
        assertEquals(ChlorineStatus.ADVERTENCIA, ChlorineRules.status(2.99))
        assertEquals(ChlorineStatus.ADVERTENCIA, ChlorineRules.status(5.01))
        assertEquals(ChlorineStatus.ADVERTENCIA, ChlorineRules.status(6.00))
    }

    @Test
    fun `critical range is below 2 or above 6 ppm`() {
        assertEquals(ChlorineStatus.CRITICO, ChlorineRules.status(1.99))
        assertEquals(ChlorineStatus.CRITICO, ChlorineRules.status(6.01))
        assertEquals(ChlorineStatus.CRITICO, ChlorineRules.status(0.00))
        assertEquals(ChlorineStatus.CRITICO, ChlorineRules.status(10.00))
        assertEquals(ChlorineStatus.CRITICO, ChlorineRules.status(12.50))
    }

    @Test
    fun `low and high deviations have different issue labels`() {
        assertEquals("Cloro insuficiente", ChlorineRules.issue(2.50))
        assertEquals("Cloro excesivo", ChlorineRules.issue(5.50))
    }

    @Test
    fun `only normal verification can close an alert`() {
        assertTrue(ChlorineRules.canCloseAlert(3.00))
        assertTrue(ChlorineRules.canCloseAlert(4.00))
        assertTrue(ChlorineRules.canCloseAlert(5.00))
        assertFalse(ChlorineRules.canCloseAlert(2.99))
        assertFalse(ChlorineRules.canCloseAlert(5.01))
    }
}
