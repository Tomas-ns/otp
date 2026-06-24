package pt.isel.otp.domain.enums

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TransportTypeTest {
    @Test
    fun `has exactly two values`() {
        assertEquals(2, TransportType.entries.size)
    }

    @Test
    fun `contains METRO`() {
        assertTrue(TransportType.entries.contains(TransportType.METRO))
    }

    @Test
    fun `contains TRAIN`() {
        assertTrue(TransportType.entries.contains(TransportType.TRAIN))
    }

    @Test
    fun `valueOf METRO`() {
        assertEquals(TransportType.METRO, TransportType.valueOf("METRO"))
    }

    @Test
    fun `valueOf TRAIN`() {
        assertEquals(TransportType.TRAIN, TransportType.valueOf("TRAIN"))
    }

    @Test
    fun `valueOf invalid throws`() {
        assertThrows(IllegalArgumentException::class.java) { TransportType.valueOf("BUS") }
    }

    @Test
    fun `METRO name is METRO`() {
        assertEquals("METRO", TransportType.METRO.name)
    }

    @Test
    fun `TRAIN name is TRAIN`() {
        assertEquals("TRAIN", TransportType.TRAIN.name)
    }
}
