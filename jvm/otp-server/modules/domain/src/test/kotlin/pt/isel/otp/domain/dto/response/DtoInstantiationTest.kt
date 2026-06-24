package pt.isel.otp.domain.dto.response

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import pt.isel.otp.domain.enums.PredictionType
import pt.isel.otp.domain.enums.TransportType
import java.util.UUID

class DtoInstantiationTest {
    @Test
    fun `UserResponse created correctly`() {
        val id = UUID.randomUUID()
        val resp = UserResponse(id = id, email = "test@test.com", displayName = "Test", avatarUrl = "http://avatar")
        assertEquals(id, resp.id)
        assertEquals("test@test.com", resp.email)
        assertEquals("Test", resp.displayName)
        assertEquals("http://avatar", resp.avatarUrl)
    }

    @Test
    fun `UserResponse with null optional fields`() {
        val id = UUID.randomUUID()
        val resp = UserResponse(id = id, email = null, displayName = null, avatarUrl = null)
        assertNull(resp.email)
        assertNull(resp.displayName)
        assertNull(resp.avatarUrl)
    }

    @Test
    fun `AuthResponse created correctly`() {
        val user = UserResponse(id = UUID.randomUUID(), email = "e", displayName = "n", avatarUrl = "a")
        val resp = AuthResponse(accessToken = "at", refreshToken = "rt", user = user)
        assertEquals("at", resp.accessToken)
        assertEquals("rt", resp.refreshToken)
        assertEquals(user, resp.user)
    }

    @Test
    fun `TelemetryResponse created correctly`() {
        val resp = TelemetryResponse(stationId = "sid", stationName = "sn", occupancyLevel = 3, predictionType = PredictionType.COMPLETE)
        assertEquals("sid", resp.stationId)
        assertEquals("sn", resp.stationName)
        assertEquals(3, resp.occupancyLevel)
        assertEquals(PredictionType.COMPLETE, resp.predictionType)
    }

    @Test
    fun `TelemetryResponse with LIMITED type`() {
        val resp = TelemetryResponse(stationId = "sid", stationName = "sn", occupancyLevel = 1, predictionType = PredictionType.LIMITED)
        assertEquals(PredictionType.LIMITED, resp.predictionType)
    }

    @Test
    fun `StationOccupancyResponse created correctly`() {
        val resp = StationOccupancyResponse(
            stationId = "sid", name = "n",
            latitude = 38.7, longitude = -9.1,
            transportType = TransportType.METRO,
            occupancyLevel = 5, predictionType = PredictionType.COMPLETE,
        )
        assertEquals("sid", resp.stationId)
        assertEquals("n", resp.name)
        assertEquals(38.7, resp.latitude)
        assertEquals(-9.1, resp.longitude)
        assertEquals(TransportType.METRO, resp.transportType)
        assertEquals(5, resp.occupancyLevel)
        assertEquals(PredictionType.COMPLETE, resp.predictionType)
    }

    @Test
    fun `StationOccupancyResponse with TRAIN type`() {
        val resp = StationOccupancyResponse(
            stationId = "tid", name = "tn",
            latitude = 38.7, longitude = -9.1,
            transportType = TransportType.TRAIN,
            occupancyLevel = 2, predictionType = PredictionType.LIMITED,
        )
        assertEquals(TransportType.TRAIN, resp.transportType)
    }

    @Test
    fun `OccupancyMapResponse created correctly`() {
        val stations = listOf(
            StationOccupancyResponse("a", "A", 1.0, 2.0, TransportType.METRO, 3, PredictionType.COMPLETE),
            StationOccupancyResponse("b", "B", 3.0, 4.0, TransportType.TRAIN, 1, PredictionType.LIMITED),
        )
        val resp = OccupancyMapResponse(stations = stations)
        assertEquals(2, resp.stations.size)
        assertEquals("a", resp.stations[0].stationId)
        assertEquals("b", resp.stations[1].stationId)
    }

    @Test
    fun `OccupancyMapResponse with empty stations`() {
        val resp = OccupancyMapResponse(stations = emptyList())
        assertTrue(resp.stations.isEmpty())
    }
}
