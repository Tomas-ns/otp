package pt.isel.otp.domain.enums

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class OAuthProviderTest {
    @Test
    fun `has exactly one value`() {
        assertEquals(1, OAuthProvider.entries.size)
    }

    @Test
    fun `contains GOOGLE`() {
        assertTrue(OAuthProvider.entries.contains(OAuthProvider.GOOGLE))
    }

    @Test
    fun `valueOf GOOGLE`() {
        assertEquals(OAuthProvider.GOOGLE, OAuthProvider.valueOf("GOOGLE"))
    }

    @Test
    fun `GOOGLE name is GOOGLE`() {
        assertEquals("GOOGLE", OAuthProvider.GOOGLE.name)
    }
}
