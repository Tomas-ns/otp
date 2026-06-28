package pt.isel

import org.junit.Assert.*
import org.junit.Test
import pt.isel.helpers.formatTime

class HelpersTest {

    @Test
    fun formatTime_zero() {
        assertEquals("00:00", formatTime(0))
    }

    @Test
    fun formatTime_oneMinute() {
        assertEquals("01:00", formatTime(60))
    }

    @Test
    fun formatTime_oneMinuteThirty() {
        assertEquals("01:30", formatTime(90))
    }

    @Test
    fun formatTime_exactlyOneHour() {
        assertEquals("60:00", formatTime(3600))
    }

    @Test
    fun formatTime_oneHourOneMinuteOneSecond() {
        assertEquals("61:01", formatTime(3661))
    }

    @Test
    fun formatTime_defaultTimeout() {
        assertEquals("15:00", formatTime(900))
    }

    @Test
    fun formatTime_largeValue() {
        assertEquals("166:40", formatTime(10000))
    }

    @Test
    fun formatTime_singleSecond() {
        assertEquals("00:01", formatTime(1))
    }

    @Test
    fun formatTime_maxInt() {
        assertEquals("35791394:07", formatTime(Int.MAX_VALUE))
    }
}
