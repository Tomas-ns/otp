package pt.isel.otp.repository

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import pt.isel.otp.domain.entity.Prediction
import pt.isel.otp.domain.entity.Station
import pt.isel.otp.domain.entity.User
import pt.isel.otp.domain.enums.PredictionType
import pt.isel.otp.domain.enums.TransportType

@DataJpaTest
class PredictionRepositoryTest {
    @Autowired
    private lateinit var predictionRepository: PredictionRepository
    @Autowired
    private lateinit var stationRepository: StationRepository
    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var station: Station
    private lateinit var otherStation: Station
    private lateinit var user: User

    @BeforeEach
    fun setup() {
        station = stationRepository.save(Station("s1", "Station1", 1.0, 2.0, TransportType.METRO))
        otherStation = stationRepository.save(Station("s2", "Station2", 3.0, 4.0, TransportType.TRAIN))
        user = userRepository.save(User(email = "u@u.com"))
    }

    @Test
    fun `save COMPLETE prediction`() {
        val p = Prediction(station = station, user = user, occupancyLevel = 4, type = PredictionType.COMPLETE)
        val saved = predictionRepository.save(p)
        assertNotNull(saved.id)
        assertEquals(4, saved.occupancyLevel)
        assertEquals(PredictionType.COMPLETE, saved.type)
    }

    @Test
    fun `save LIMITED prediction without user`() {
        val p = Prediction(station = station, user = null, occupancyLevel = 2, type = PredictionType.LIMITED)
        val saved = predictionRepository.save(p)
        assertNull(saved.user)
        assertEquals(PredictionType.LIMITED, saved.type)
    }

    @Test
    fun `findFirstByStation_IdAndTypeOrderByCreatedAtDesc returns latest`() {
        predictionRepository.save(Prediction(station = station, user = user, occupancyLevel = 2, type = PredictionType.COMPLETE))
        Thread.sleep(10)
        predictionRepository.save(Prediction(station = station, user = user, occupancyLevel = 5, type = PredictionType.COMPLETE))
        val latest = predictionRepository.findFirstByStation_IdAndTypeOrderByCreatedAtDesc(station.id, PredictionType.COMPLETE)
        assertTrue(latest.isPresent)
        assertEquals(5, latest.get().occupancyLevel)
    }

    @Test
    fun `findFirstByStation_IdAndTypeOrderByCreatedAtDesc returns empty for unknown station`() {
        val result = predictionRepository.findFirstByStation_IdAndTypeOrderByCreatedAtDesc("unknown", PredictionType.COMPLETE)
        assertFalse(result.isPresent)
    }

    @Test
    fun `findFirstByStation_IdAndTypeOrderByCreatedAtDesc returns empty for mismatched type`() {
        predictionRepository.save(Prediction(station = station, user = user, occupancyLevel = 3, type = PredictionType.COMPLETE))
        val result = predictionRepository.findFirstByStation_IdAndTypeOrderByCreatedAtDesc(station.id, PredictionType.LIMITED)
        assertFalse(result.isPresent)
    }

    @Test
    fun `findLatestByType COMPLETE returns latest per station`() {
        predictionRepository.save(Prediction(station = station, user = user, occupancyLevel = 3, type = PredictionType.COMPLETE))
        Thread.sleep(10)
        predictionRepository.save(Prediction(station = station, user = user, occupancyLevel = 4, type = PredictionType.COMPLETE))
        predictionRepository.save(Prediction(station = otherStation, user = user, occupancyLevel = 2, type = PredictionType.COMPLETE))
        val results = predictionRepository.findLatestByType(PredictionType.COMPLETE)
        assertEquals(2, results.size)
        val s1 = results.find { it.station.id == station.id }
        assertEquals(4, s1?.occupancyLevel)
    }

    @Test
    fun `findLatestByType LIMITED returns latest per station`() {
        predictionRepository.save(Prediction(station = station, user = null, occupancyLevel = 1, type = PredictionType.LIMITED))
        predictionRepository.save(Prediction(station = otherStation, user = null, occupancyLevel = 3, type = PredictionType.LIMITED))
        val results = predictionRepository.findLatestByType(PredictionType.LIMITED)
        assertEquals(2, results.size)
    }

    @Test
    fun `findLatestByType returns empty list when no predictions exist`() {
        val results = predictionRepository.findLatestByType(PredictionType.COMPLETE)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `findLatestByType for COMPLETE does not include LIMITED`() {
        predictionRepository.save(Prediction(station = station, user = null, occupancyLevel = 2, type = PredictionType.LIMITED))
        val results = predictionRepository.findLatestByType(PredictionType.COMPLETE)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `save multiple predictions and find by station`() {
        predictionRepository.save(Prediction(station = station, user = user, occupancyLevel = 1, type = PredictionType.COMPLETE))
        predictionRepository.save(Prediction(station = station, user = user, occupancyLevel = 2, type = PredictionType.COMPLETE))
        predictionRepository.save(Prediction(station = station, user = null, occupancyLevel = 3, type = PredictionType.LIMITED))
        val latestComplete = predictionRepository.findFirstByStation_IdAndTypeOrderByCreatedAtDesc(station.id, PredictionType.COMPLETE)
        assertTrue(latestComplete.isPresent)
        assertEquals(2, latestComplete.get().occupancyLevel)
    }
}
