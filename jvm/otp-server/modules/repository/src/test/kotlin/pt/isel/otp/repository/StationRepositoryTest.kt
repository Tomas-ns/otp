package pt.isel.otp.repository

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import pt.isel.otp.domain.entity.Station
import pt.isel.otp.domain.enums.TransportType

@DataJpaTest
class StationRepositoryTest {
    @Autowired
    private lateinit var stationRepository: StationRepository

    @Test
    fun `save and find station by id`() {
        val station = Station(id = "test_1", name = "Test", latitude = 38.7, longitude = -9.1, transportType = TransportType.METRO)
        stationRepository.save(station)
        val found = stationRepository.findById("test_1")
        assertTrue(found.isPresent)
        assertEquals("Test", found.get().name)
    }

    @Test
    fun `findAll returns all stations`() {
        stationRepository.save(Station("a", "A", 1.0, 2.0, TransportType.METRO))
        stationRepository.save(Station("b", "B", 3.0, 4.0, TransportType.TRAIN))
        assertEquals(2, stationRepository.findAll().size)
    }

    @Test
    fun `findById returns empty for unknown id`() {
        val found = stationRepository.findById("nonexistent")
        assertFalse(found.isPresent)
    }

    @Test
    fun `delete station`() {
        val station = Station("del", "Del", 1.0, 2.0, TransportType.METRO)
        stationRepository.save(station)
        stationRepository.delete(station)
        assertFalse(stationRepository.findById("del").isPresent)
    }

    @Test
    fun `save station with TRAIN type`() {
        val station = Station("train_1", "Train", 38.7, -9.1, TransportType.TRAIN)
        stationRepository.save(station)
        val found = stationRepository.findById("train_1")
        assertEquals(TransportType.TRAIN, found.get().transportType)
    }

    @Test
    fun `existsById returns correct value`() {
        stationRepository.save(Station("exist", "Exist", 1.0, 2.0, TransportType.METRO))
        assertTrue(stationRepository.existsById("exist"))
        assertFalse(stationRepository.existsById("noexist"))
    }

    @Test
    fun `count returns correct number`() {
        stationRepository.save(Station("c1", "C1", 1.0, 2.0, TransportType.METRO))
        stationRepository.save(Station("c2", "C2", 3.0, 4.0, TransportType.TRAIN))
        assertEquals(2, stationRepository.count())
    }
}
