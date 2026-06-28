package pt.isel

import org.junit.Assert.*
import org.junit.Test
import pt.isel.datascan.viewmodel.state.*

class ConstantsTest {

    @Test
    fun defaultTimeout_is15MinutesInSeconds() {
        assertEquals(900, DEFAULT_TIMEOUT)
    }

    @Test
    fun defaultInterval_is32Seconds() {
        assertEquals(32, DEFAULT_INTERVAL)
    }

    @Test
    fun isTestTrip_defaultIsFalse() {
        assertFalse(IS_TEST_TRIP)
    }

    @Test
    fun notificationReminderInterval_is3MinutesInSeconds() {
        assertEquals(180, NOTIFICATION_REMINDER_INTERVAL)
    }

    @Test
    fun defaultSubjectiveRating_is3() {
        assertEquals(3, DEFAULT_SUBJ_RATING)
    }
}
