package pt.isel.otp.domain.enums

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UserStatusTest {
    @Test
    fun `has exactly three values`() {
        assertEquals(3, UserStatus.entries.size)
    }

    @Test
    fun `contains ACTIVE`() {
        assertTrue(UserStatus.entries.contains(UserStatus.ACTIVE))
    }

    @Test
    fun `contains DISABLED`() {
        assertTrue(UserStatus.entries.contains(UserStatus.DISABLED))
    }

    @Test
    fun `contains DELETED`() {
        assertTrue(UserStatus.entries.contains(UserStatus.DELETED))
    }

    @Test
    fun `valueOf ACTIVE`() {
        assertEquals(UserStatus.ACTIVE, UserStatus.valueOf("ACTIVE"))
    }

    @Test
    fun `valueOf DISABLED`() {
        assertEquals(UserStatus.DISABLED, UserStatus.valueOf("DISABLED"))
    }

    @Test
    fun `valueOf DELETED`() {
        assertEquals(UserStatus.DELETED, UserStatus.valueOf("DELETED"))
    }

    @Test
    fun `ACTIVE name is ACTIVE`() {
        assertEquals("ACTIVE", UserStatus.ACTIVE.name)
    }
}
