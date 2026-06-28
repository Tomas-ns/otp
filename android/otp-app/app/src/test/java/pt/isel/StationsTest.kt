package pt.isel

import org.junit.Assert.*
import org.junit.Test
import pt.isel.domain.Station
import pt.isel.domain.TransportType
import pt.isel.domain.metroStations
import pt.isel.domain.trainStations

class StationsTest {

    @Test
    fun metroStations_shouldHave50Stations() {
        assertEquals(50, metroStations.size)
    }

    @Test
    fun trainStations_shouldHave44Stations() {
        assertEquals(44, trainStations.size)
    }

    @Test
    fun totalStations_shouldBe94() {
        assertEquals(94, metroStations.size + trainStations.size)
    }

    @Test
    fun allMetroStations_shouldBeMetroType() {
        metroStations.forEach { assertEquals(TransportType.METRO, it.type) }
    }

    @Test
    fun allTrainStations_shouldBeTrainType() {
        trainStations.forEach { assertEquals(TransportType.TRAIN, it.type) }
    }

    @Test
    fun noStationIdShouldBeNullOrEmpty() {
        val all = metroStations + trainStations
        all.forEach { s ->
            assertNotNull(s.stationId)
            assertTrue(s.stationId.isNotBlank())
        }
    }

    @Test
    fun noStationNameShouldBeNullOrEmpty() {
        val all = metroStations + trainStations
        all.forEach { s ->
            assertNotNull(s.name)
            assertTrue(s.name.isNotBlank())
        }
    }

    @Test
    fun stationIds_shouldBeUnique() {
        val all = metroStations + trainStations
        val ids = all.map { it.stationId }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun firstMetroStation_shouldBeReboleira() {
        val first = metroStations.first()
        assertEquals("Reboleira", first.name)
        assertEquals("reboleira_metro", first.stationId)
    }

    @Test
    fun lastMetroStation_shouldBeAeroporto() {
        val last = metroStations.last()
        assertEquals("Aeroporto", last.name)
        assertEquals("aeroporto_metro", last.stationId)
    }

    @Test
    fun firstTrainStation_shouldBeCruzQuebrada() {
        val first = trainStations.first()
        assertEquals("Cruz Quebrada", first.name)
        assertEquals("cruz_quebrada_train", first.stationId)
    }

    @Test
    fun lastTrainStation_shouldBeAgualvaCacem() {
        val last = trainStations.last()
        assertEquals("Agualva Cacém", last.name)
        assertEquals("agualva_cacem_train", last.stationId)
    }

    @Test
    fun metroStationCoordinates_shouldBeValid() {
        metroStations.forEach { s ->
            assertTrue("Latitude ${s.name} out of range", s.location.latitude in 38.0..39.0)
            assertTrue("Longitude ${s.name} out of range", s.location.longitude in -10.0..-8.0)
        }
    }

    @Test
    fun trainStationCoordinates_shouldBeValid() {
        trainStations.forEach { s ->
            assertTrue("Latitude ${s.name} out of range", s.location.latitude in 38.0..39.0)
            assertTrue("Longitude ${s.name} out of range", s.location.longitude in -10.0..-8.0)
        }
    }

    @Test
    fun stationIds_shouldEndWithTransportSuffix() {
        metroStations.forEach { s ->
            assertTrue("${s.stationId} should end with _metro", s.stationId.endsWith("_metro"))
        }
        trainStations.forEach { s ->
            assertTrue("${s.stationId} should end with _train", s.stationId.endsWith("_train"))
        }
    }
}
