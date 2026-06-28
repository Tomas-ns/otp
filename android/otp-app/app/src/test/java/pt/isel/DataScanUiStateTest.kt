package pt.isel

import org.junit.Assert.*
import org.junit.Test
import pt.isel.datascan.viewmodel.state.DataScanUiState
import pt.isel.datascan.viewmodel.state.DEFAULT_SUBJ_RATING
import pt.isel.datascan.viewmodel.state.DEFAULT_TIMEOUT

class DataScanUiStateTest {

    @Test
    fun uiState_defaultValues() {
        val state = DataScanUiState()
        assertFalse(state.isRiding)
        assertFalse(state.isPaused)
        assertFalse(state.isAwaitingInitialRating)
        assertEquals(DEFAULT_TIMEOUT, state.secondsRemaining)
        assertNull(state.tripId)
        assertEquals(DEFAULT_SUBJ_RATING, state.currentSubjectiveRating)
        assertNull(state.lastRead)
        assertNull(state.finishedTripIdToConfirm)
    }

    @Test
    fun uiState_isRidingTrue() {
        val state = DataScanUiState(isRiding = true)
        assertTrue(state.isRiding)
    }

    @Test
    fun uiState_isPausedTrue() {
        val state = DataScanUiState(isPaused = true)
        assertTrue(state.isPaused)
    }

    @Test
    fun uiState_isAwaitingInitialRatingTrue() {
        val state = DataScanUiState(isAwaitingInitialRating = true)
        assertTrue(state.isAwaitingInitialRating)
    }

    @Test
    fun uiState_customSecondsRemaining() {
        val state = DataScanUiState(secondsRemaining = 500)
        assertEquals(500, state.secondsRemaining)
    }

    @Test
    fun uiState_withTripId() {
        val state = DataScanUiState(tripId = "trip_123")
        assertEquals("trip_123", state.tripId)
    }

    @Test
    fun uiState_customRating() {
        val state = DataScanUiState(currentSubjectiveRating = 5)
        assertEquals(5, state.currentSubjectiveRating)
    }

    @Test
    fun uiState_withFinishedTripId() {
        val state = DataScanUiState(finishedTripIdToConfirm = "trip_999")
        assertEquals("trip_999", state.finishedTripIdToConfirm)
    }

    @Test
    fun uiState_copyShouldPreserveOtherFields() {
        val state = DataScanUiState(isRiding = true, tripId = "t_1")
        val copy = state.copy(isPaused = true)
        assertTrue(copy.isRiding)
        assertTrue(copy.isPaused)
        assertEquals("t_1", copy.tripId)
    }
}
