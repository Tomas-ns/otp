package pt.isel

import org.junit.Assert.*
import org.junit.Test
import org.osmdroid.util.GeoPoint
import pt.isel.datascan.domain.TransportationType
import pt.isel.domain.*
import java.util.UUID

class DomainEntitiesTest {

    // ---------- AuthResponse ----------
    @Test
    fun authResponse_shouldCreateWithAllFields() {
        val user = UserResponse(UUID.randomUUID(), "e@mail.com", "Dsp", "url")
        val auth = AuthResponse("access", "refresh", user)
        assertEquals("access", auth.accessToken)
        assertEquals("refresh", auth.refreshToken)
        assertEquals(user, auth.user)
    }

    @Test
    fun authResponse_shouldHandleNullEmail() {
        val user = UserResponse(UUID.randomUUID(), null, "Dsp", "url")
        val auth = AuthResponse("a", "r", user)
        assertNull(auth.user.email)
        assertEquals("Dsp", auth.user.displayName)
    }

    // ---------- OccupancyMapResponse ----------
    @Test
    fun occupancyMapResponse_shouldCreateWithStations() {
        val stations = listOf(
            StationOccupancyResponse("s1", "St1", 38.7, -9.1, TransportType.METRO, 3, PredictionType.COMPLETE),
            StationOccupancyResponse("s2", "St2", 38.8, -9.2, TransportType.TRAIN, 1, PredictionType.LIMITED)
        )
        val resp = OccupancyMapResponse(stations)
        assertEquals(2, resp.stations.size)
        assertEquals("St1", resp.stations[0].name)
        assertEquals(1, resp.stations[1].occupancyLevel)
    }

    @Test
    fun occupancyMapResponse_shouldAllowEmptyStations() {
        val resp = OccupancyMapResponse(emptyList())
        assertTrue(resp.stations.isEmpty())
    }

    // ---------- PredictionType ----------
    @Test
    fun predictionType_shouldHaveExpectedValues() {
        assertEquals(2, PredictionType.entries.size)
        assertTrue(PredictionType.entries.contains(PredictionType.COMPLETE))
        assertTrue(PredictionType.entries.contains(PredictionType.LIMITED))
    }

    @Test
    fun predictionType_COMPLETE_ordinalIsZero() {
        assertEquals(0, PredictionType.COMPLETE.ordinal)
    }

    @Test
    fun predictionType_LIMITED_ordinalIsOne() {
        assertEquals(1, PredictionType.LIMITED.ordinal)
    }

    @Test
    fun predictionType_valueOf_shouldWork() {
        assertEquals(PredictionType.COMPLETE, PredictionType.valueOf("COMPLETE"))
        assertEquals(PredictionType.LIMITED, PredictionType.valueOf("LIMITED"))
    }

    // ---------- StationOccupancyResponse ----------
    @Test
    fun stationOccupancyResponse_shouldCreateWithAllFields() {
        val s = StationOccupancyResponse("id1", "Station 1", 38.7, -9.1, TransportType.METRO, 3, PredictionType.COMPLETE)
        assertEquals("id1", s.stationId)
        assertEquals("Station 1", s.name)
        assertEquals(38.7, s.latitude, 0.001)
        assertEquals(-9.1, s.longitude, 0.001)
        assertEquals(TransportType.METRO, s.transportType)
        assertEquals(3, s.occupancyLevel)
        assertEquals(PredictionType.COMPLETE, s.predictionType)
    }

    @Test
    fun stationOccupancyResponse_shouldHandleMinOccupancy() {
        val s = StationOccupancyResponse("id", "St", 0.0, 0.0, TransportType.TRAIN, 0, PredictionType.LIMITED)
        assertEquals(0, s.occupancyLevel)
    }

    @Test
    fun stationOccupancyResponse_shouldHandleMaxOccupancy() {
        val s = StationOccupancyResponse("id", "St", 0.0, 0.0, TransportType.TRAIN, 5, PredictionType.LIMITED)
        assertEquals(5, s.occupancyLevel)
    }

    // ---------- Station ----------
    @Test
    fun station_shouldCreateWithAllFields() {
        val loc = GeoPoint(38.7, -9.1)
        val s = Station("Station 1", "s1", TransportType.METRO, loc)
        assertEquals("Station 1", s.name)
        assertEquals("s1", s.stationId)
        assertEquals(TransportType.METRO, s.type)
        assertEquals(loc, s.location)
    }

    @Test
    fun station_dataClassEquality() {
        val loc = GeoPoint(38.7, -9.1)
        val s1 = Station("A", "a1", TransportType.METRO, loc)
        val s2 = Station("A", "a1", TransportType.METRO, GeoPoint(38.7, -9.1))
        assertEquals(s1, s2)
        assertEquals(s1.hashCode(), s2.hashCode())
    }

    @Test
    fun station_dataClassInequality() {
        val loc = GeoPoint(38.7, -9.1)
        val s1 = Station("A", "a1", TransportType.METRO, loc)
        val s2 = Station("B", "a1", TransportType.METRO, loc)
        assertNotEquals(s1, s2)
    }

    // ---------- TransportType ----------
    @Test
    fun transportType_shouldHaveExpectedValues() {
        assertEquals(2, TransportType.entries.size)
        assertTrue(TransportType.entries.contains(TransportType.METRO))
        assertTrue(TransportType.entries.contains(TransportType.TRAIN))
    }

    @Test
    fun transportType_valueOf_shouldWork() {
        assertEquals(TransportType.METRO, TransportType.valueOf("METRO"))
        assertEquals(TransportType.TRAIN, TransportType.valueOf("TRAIN"))
    }

    @Test
    fun transportType_METRO_ordinalIsZero() {
        assertEquals(0, TransportType.METRO.ordinal)
    }

    // ---------- TransportationType ----------
    @Test
    fun transportationType_shouldHaveExpectedValues() {
        assertEquals(2, TransportationType.entries.size)
        assertTrue(TransportationType.entries.contains(TransportationType.TRAIN))
        assertTrue(TransportationType.entries.contains(TransportationType.METRO))
    }

    @Test
    fun transportationType_valueOf_shouldWork() {
        assertEquals(TransportationType.METRO, TransportationType.valueOf("METRO"))
        assertEquals(TransportationType.TRAIN, TransportationType.valueOf("TRAIN"))
    }

    @Test
    fun transportationType_TRAIN_ordinalIsZero() {
        assertEquals(0, TransportationType.TRAIN.ordinal)
    }

    @Test
    fun transportationType_METRO_ordinalIsOne() {
        assertEquals(1, TransportationType.METRO.ordinal)
    }

    // ---------- UserResponse ----------
    @Test
    fun userResponse_shouldCreateWithAllFields() {
        val id = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val u = UserResponse(id, "user@mail.com", "User Name", "http://avatar.url")
        assertEquals(id, u.id)
        assertEquals("user@mail.com", u.email)
        assertEquals("User Name", u.displayName)
        assertEquals("http://avatar.url", u.avatarUrl)
    }

    @Test
    fun userResponse_shouldAllowNullFields() {
        val u = UserResponse(UUID.randomUUID(), null, null, null)
        assertNull(u.email)
        assertNull(u.displayName)
        assertNull(u.avatarUrl)
    }
}
