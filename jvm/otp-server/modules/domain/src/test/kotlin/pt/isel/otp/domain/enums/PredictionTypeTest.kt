package pt.isel.otp.domain.enums

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PredictionTypeTest {
    @Test
    fun `has exactly two values`() {
        assertEquals(2, PredictionType.entries.size)
    }

    @Test
    fun `contains COMPLETE`() {
        assertTrue(PredictionType.entries.contains(PredictionType.COMPLETE))
    }

    @Test
    fun `contains LIMITED`() {
        assertTrue(PredictionType.entries.contains(PredictionType.LIMITED))
    }

    @Test
    fun `valueOf COMPLETE`() {
        assertEquals(PredictionType.COMPLETE, PredictionType.valueOf("COMPLETE"))
    }

    @Test
    fun `valueOf LIMITED`() {
        assertEquals(PredictionType.LIMITED, PredictionType.valueOf("LIMITED"))
    }

    @Test
    fun `COMPLETE name is COMPLETE`() {
        assertEquals("COMPLETE", PredictionType.COMPLETE.name)
    }

    @Test
    fun `LIMITED name is LIMITED`() {
        assertEquals("LIMITED", PredictionType.LIMITED.name)
    }
}
