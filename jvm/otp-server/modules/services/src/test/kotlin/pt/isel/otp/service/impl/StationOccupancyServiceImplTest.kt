package pt.isel.otp.service.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import pt.isel.otp.domain.entity.Prediction
import pt.isel.otp.domain.entity.Station
import pt.isel.otp.domain.enums.PredictionType
import pt.isel.otp.domain.enums.TransportType
import pt.isel.otp.repository.PredictionRepository
import pt.isel.otp.repository.StationRepository
import pt.isel.otp.service.InferenceService
import java.util.*

@ExtendWith(MockitoExtension::class)
class StationOccupancyServiceImplTest {
    @Mock private lateinit var stationRepository: StationRepository
    @Mock private lateinit var predictionRepository: PredictionRepository
    @Mock private lateinit var inferenceService: InferenceService

    private lateinit var stationOccupancyService: StationOccupancyServiceImpl

    private val station = Station("alameda_metro", "Alameda", 38.7371, -9.1329, TransportType.METRO)
    private val timestamp = 1717776000L

    @BeforeEach
    fun setup() {
        stationOccupancyService = StationOccupancyServiceImpl(stationRepository, predictionRepository, inferenceService)
    }

    @Test
    fun `getLimitedPrediction returns LIMITED prediction for existing station`() {
        `when`(stationRepository.findById("alameda_metro")).thenReturn(Optional.of(station))
        `when`(inferenceService.predictLimited(station, timestamp)).thenReturn(3)
        val response = stationOccupancyService.getLimitedPrediction("alameda_metro", timestamp)
        assertEquals("alameda_metro", response.stationId)
        assertEquals("Alameda", response.name)
        assertEquals(3, response.occupancyLevel)
        assertEquals(PredictionType.LIMITED, response.predictionType)
        verify(predictionRepository).save(any<Prediction>())
    }

    @Test
    fun `getLimitedPrediction saves prediction without user`() {
        `when`(stationRepository.findById("alameda_metro")).thenReturn(Optional.of(station))
        `when`(inferenceService.predictLimited(station, timestamp)).thenReturn(2)
        stationOccupancyService.getLimitedPrediction("alameda_metro", timestamp)
        verify(predictionRepository).save(argThat { p -> p.user == null && p.type == PredictionType.LIMITED })
    }

    @Test
    fun `getLimitedPrediction throws for unknown station`() {
        `when`(stationRepository.findById("unknown")).thenReturn(Optional.empty())
        assertThrows(NoSuchElementException::class.java) {
            stationOccupancyService.getLimitedPrediction("unknown", timestamp)
        }
    }

    @Test
    fun `getLimitedPrediction handles all occupancy levels`() {
        `when`(stationRepository.findById("alameda_metro")).thenReturn(Optional.of(station))
        for (level in 1..5) {
            `when`(inferenceService.predictLimited(station, timestamp)).thenReturn(level)
            val response = stationOccupancyService.getLimitedPrediction("alameda_metro", timestamp)
            assertEquals(level, response.occupancyLevel)
        }
    }

    @Test
    fun `getLimitedPrediction returns station coordinates`() {
        `when`(stationRepository.findById("alameda_metro")).thenReturn(Optional.of(station))
        `when`(inferenceService.predictLimited(station, timestamp)).thenReturn(4)
        val response = stationOccupancyService.getLimitedPrediction("alameda_metro", timestamp)
        assertEquals(38.7371, response.latitude)
        assertEquals(-9.1329, response.longitude)
    }

    @Test
    fun `getLimitedPrediction returns TransportType`() {
        `when`(stationRepository.findById("alameda_metro")).thenReturn(Optional.of(station))
        `when`(inferenceService.predictLimited(station, timestamp)).thenReturn(1)
        val response = stationOccupancyService.getLimitedPrediction("alameda_metro", timestamp)
        assertEquals(TransportType.METRO, response.transportType)
    }

    @Test
    fun `getLimitedPrediction works with TRAIN station`() {
        val trainStation = Station("cascais_train", "Cascais", 38.7009, -9.4179, TransportType.TRAIN)
        `when`(stationRepository.findById("cascais_train")).thenReturn(Optional.of(trainStation))
        `when`(inferenceService.predictLimited(trainStation, timestamp)).thenReturn(5)
        val response = stationOccupancyService.getLimitedPrediction("cascais_train", timestamp)
        assertEquals(TransportType.TRAIN, response.transportType)
    }

    @Test
    fun `getLimitedPrediction uses different timestamps`() {
        `when`(stationRepository.findById("alameda_metro")).thenReturn(Optional.of(station))
        val ts1 = 1717776000L
        val ts2 = 1717862400L
        `when`(inferenceService.predictLimited(station, ts1)).thenReturn(2)
        `when`(inferenceService.predictLimited(station, ts2)).thenReturn(4)
        assertEquals(2, stationOccupancyService.getLimitedPrediction("alameda_metro", ts1).occupancyLevel)
        assertEquals(4, stationOccupancyService.getLimitedPrediction("alameda_metro", ts2).occupancyLevel)
    }
}
