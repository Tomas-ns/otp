package pt.isel

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import pt.isel.settings.domain.repository.MockSettingsRepository
import pt.isel.settings.viewmodel.SettingsViewModel

class ViewModelsTest {

    // ---------- SettingsViewModel ----------
    @Test
    fun settingsViewModel_shouldExposeRepositoryFlows() = runBlocking {
        val repo = MockSettingsRepository()
        val vm = SettingsViewModel(repo)
        assertEquals(repo.timeout.first(), vm.timeout.first())
        assertEquals(repo.interval.first(), vm.interval.first())
        assertEquals(repo.isTestTrip.first(), vm.isTestTrip.first())
        assertEquals(repo.notificationInterval.first(), vm.notificationInterval.first())
        assertEquals(repo.userId.first(), vm.userId.first())
    }

    @Test
    fun settingsViewModel_timeoutFlowDefault() = runBlocking {
        val vm = SettingsViewModel(MockSettingsRepository())
        assertEquals(900, vm.timeout.first())
    }

    @Test
    fun settingsViewModel_intervalFlowDefault() = runBlocking {
        val vm = SettingsViewModel(MockSettingsRepository())
        assertEquals(32, vm.interval.first())
    }

    @Test
    fun settingsViewModel_isTestTripDefault() = runBlocking {
        val vm = SettingsViewModel(MockSettingsRepository())
        assertFalse(vm.isTestTrip.first())
    }

    @Test
    fun settingsViewModel_notificationIntervalDefault() = runBlocking {
        val vm = SettingsViewModel(MockSettingsRepository())
        assertEquals(180, vm.notificationInterval.first())
    }

    @Test
    fun settingsViewModel_userIdDefault() = runBlocking {
        val vm = SettingsViewModel(MockSettingsRepository())
        assertEquals("test_user", vm.userId.first())
    }

    @Test
    fun settingsViewModel_multipleInstancesAreIndependent() = runBlocking {
        val repo1 = MockSettingsRepository()
        val repo2 = MockSettingsRepository()
        val vm1 = SettingsViewModel(repo1)
        val vm2 = SettingsViewModel(repo2)
        assertEquals(vm1.timeout.first(), vm2.timeout.first())
    }

    // ---------- DataScanViewModel ----------
    // DataScanViewModel requires viewModelScope in init (needs Android runtime),
    // and PlanTripViewModel/MapViewModel require ApiAccess (needs Android engine).
    // These are tested via instrumented tests (androidTest).
}
