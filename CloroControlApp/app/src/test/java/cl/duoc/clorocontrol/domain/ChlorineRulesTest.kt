package cl.duoc.clorocontrol.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChlorineRulesTest {
    @Test
    fun `normal range includes 3 and 5 ppm`() {
        assertEquals(ChlorineStatus.NORMAL, ChlorineRules.status(3.0))
        assertEquals(ChlorineStatus.NORMAL, ChlorineRules.status(5.0))
    }

    @Test
    fun `warning range includes 2 and 6 ppm`() {
        assertEquals(ChlorineStatus.ADVERTENCIA, ChlorineRules.status(2.0))
        assertEquals(ChlorineStatus.ADVERTENCIA, ChlorineRules.status(2.99))
        assertEquals(ChlorineStatus.ADVERTENCIA, ChlorineRules.status(5.01))
        assertEquals(ChlorineStatus.ADVERTENCIA, ChlorineRules.status(6.0))
    }

    @Test
    fun `critical range is outside 2 to 6 ppm`() {
        assertEquals(ChlorineStatus.CRITICO, ChlorineRules.status(1.99))
        assertEquals(ChlorineStatus.CRITICO, ChlorineRules.status(6.01))
    }

    @Test
    fun `only normal verification can close an alert`() {
        assertTrue(ChlorineRules.canCloseAlert(4.0))
        assertFalse(ChlorineRules.canCloseAlert(2.8))
        assertFalse(ChlorineRules.canCloseAlert(5.8))
    }
}
