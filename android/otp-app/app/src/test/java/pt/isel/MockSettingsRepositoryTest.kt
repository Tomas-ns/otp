package pt.isel

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import pt.isel.datascan.viewmodel.state.*
import pt.isel.settings.domain.repository.MockSettingsRepository

class MockSettingsRepositoryTest {

    private val repo = MockSettingsRepository()

    @Test
    fun mockTimeout_shouldBeDefault() = runBlocking {
        assertEquals(DEFAULT_TIMEOUT, repo.timeout.first())
    }

    @Test
    fun mockInterval_shouldBeDefault() = runBlocking {
        assertEquals(DEFAULT_INTERVAL, repo.interval.first())
    }

    @Test
    fun mockIsTestTrip_shouldBeFalse() = runBlocking {
        assertFalse(repo.isTestTrip.first())
    }

    @Test
    fun mockNotificationInterval_shouldBeDefault() = runBlocking {
        assertEquals(NOTIFICATION_REMINDER_INTERVAL, repo.notificationInterval.first())
    }

    @Test
    fun mockUserId_shouldBeTestUser() = runBlocking {
        assertEquals("test_user", repo.userId.first())
    }

    @Test
    fun updateMethods_shouldNotThrow() = runBlocking {
        repo.updateTimeout(100)
        repo.updateInterval(10)
        repo.updateIsTestTrip(true)
        repo.updateNotificationInterval(60)
        repo.createUserId()
    }
}
