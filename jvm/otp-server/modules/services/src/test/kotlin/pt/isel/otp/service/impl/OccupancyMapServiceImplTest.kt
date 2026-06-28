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

@ExtendWith(MockitoExtension::class)
class OccupancyMapServiceImplTest {
    @Mock private lateinit var predictionRepository: PredictionRepository
    @Mock private lateinit var stationRepository: StationRepository

    private lateinit var occupancyMapService: OccupancyMapServiceImpl

    private val s1 = Station("s1", "Station1", 1.0, 2.0, TransportType.METRO)
    private val s2 = Station("s2", "Station2", 3.0, 4.0, TransportType.TRAIN)
    private val s3 = Station("s3", "Station3", 5.0, 6.0, TransportType.METRO)

    @BeforeEach
    fun setup() {
        occupancyMapService = OccupancyMapServiceImpl(predictionRepository, stationRepository)
    }

    @Test
    fun `getOccupancyMap returns only stations with COMPLETE predictions`() {
        `when`(stationRepository.findAll()).thenReturn(listOf(s1, s2))
        `when`(predictionRepository.findLatestByType(PredictionType.COMPLETE)).thenReturn(
            listOf(Prediction(station = s1, user = mock(), occupancyLevel = 4, type = PredictionType.COMPLETE))
        )
        val map = occupancyMapService.getOccupancyMap()
        assertEquals(1, map.stations.size)
        assertEquals("s1", map.stations[0].stationId)
        assertEquals(4, map.stations[0].occupancyLevel)
        assertEquals(PredictionType.COMPLETE, map.stations[0].predictionType)
    }

    @Test
    fun `getOccupancyMap returns empty list when no predictions exist`() {
        `when`(stationRepository.findAll()).thenReturn(listOf(s1))
        `when`(predictionRepository.findLatestByType(PredictionType.COMPLETE)).thenReturn(emptyList())
        val map = occupancyMapService.getOccupancyMap()
        assertEquals(0, map.stations.size)
    }

    @Test
    fun `getOccupancyMap returns only stations with predictions when some have none`() {
        `when`(stationRepository.findAll()).thenReturn(listOf(s1, s2))
        `when`(predictionRepository.findLatestByType(PredictionType.COMPLETE)).thenReturn(
            listOf(Prediction(station = s1, user = mock(), occupancyLevel = 3, type = PredictionType.COMPLETE))
        )
        val map = occupancyMapService.getOccupancyMap()
        assertEquals(1, map.stations.size)
        assertEquals("s1", map.stations[0].stationId)
        assertEquals(3, map.stations[0].occupancyLevel)
    }

    @Test
    fun `getOccupancyMap returns empty when findAll returns empty`() {
        `when`(stationRepository.findAll()).thenReturn(emptyList())
        `when`(predictionRepository.findLatestByType(PredictionType.COMPLETE)).thenReturn(emptyList())
        val map = occupancyMapService.getOccupancyMap()
        assertEquals(0, map.stations.size)
    }
}
