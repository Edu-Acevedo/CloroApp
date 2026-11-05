package cl.duoc.clorocontrol.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserSessionTest {
    @Test
    fun `operario only sees assigned farm and can register`() {
        val session = UserSession("operario@demo.cl", UserRole.OPERARIO)
        assertTrue(session.canAccessFarm("Granja Norte"))
        assertFalse(session.canAccessFarm("Granja Sur"))
        assertTrue(session.canRegisterMeasurements)
        assertTrue(session.canRegisterActions)
        assertFalse(session.canValidateActions)
    }

    @Test
    fun `supervisor access follows its assigned demo farms and can validate actions`() {
        val session = UserSession("supervisor@demo.cl", UserRole.SUPERVISOR)
        assertEquals(setOf("Granja Norte"), session.assignedFarms)
        assertTrue(session.canValidateActions)
    }

    @Test
    fun `demo dataset contains fifteen accounts distributed across three roles`() {
        assertEquals(15, DemoAccounts.accounts.size)
        assertEquals(5, DemoAccounts.accounts.count { it.role == UserRole.OPERARIO })
        assertEquals(5, DemoAccounts.accounts.count { it.role == UserRole.SUPERVISOR })
        assertEquals(5, DemoAccounts.accounts.count { it.role == UserRole.JEFATURA })
        assertEquals(UserRole.OPERARIO, UserRole.fromDemoEmail("operario03@demo.cl"))
        assertTrue(UserSession("operario03@demo.cl", UserRole.OPERARIO).canAccessFarm("Granja Central"))
    }

    @Test
    fun `management is read only`() {
        val session = UserSession("jefatura@demo.cl", UserRole.JEFATURA)
        assertFalse(session.canRegisterMeasurements)
        assertFalse(session.canRegisterActions)
        assertFalse(session.canValidateActions)
    }
}
