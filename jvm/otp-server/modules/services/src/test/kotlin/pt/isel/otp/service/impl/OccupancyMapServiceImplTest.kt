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
    fun `getOccupancyMap returns COMPLETE predictions when available`() {
        `when`(stationRepository.findAll()).thenReturn(listOf(s1, s2))
        `when`(predictionRepository.findLatestByType(PredictionType.COMPLETE)).thenReturn(
            listOf(Prediction(station = s1, user = mock(), occupancyLevel = 4, type = PredictionType.COMPLETE))
        )
        `when`(predictionRepository.findLatestByType(PredictionType.LIMITED)).thenReturn(emptyList())
        val map = occupancyMapService.getOccupancyMap()
        assertEquals(2, map.stations.size)
        assertEquals(4, map.stations.find { it.stationId == "s1" }?.occupancyLevel)
        assertEquals(PredictionType.COMPLETE, map.stations.find { it.stationId == "s1" }?.predictionType)
    }

    @Test
    fun `getOccupancyMap falls back to LIMITED when no COMPLETE`() {
        `when`(stationRepository.findAll()).thenReturn(listOf(s1))
        `when`(predictionRepository.findLatestByType(PredictionType.COMPLETE)).thenReturn(emptyList())
        `when`(predictionRepository.findLatestByType(PredictionType.LIMITED)).thenReturn(
            listOf(Prediction(station = s1, user = null, occupancyLevel = 2, type = PredictionType.LIMITED))
        )
        val map = occupancyMapService.getOccupancyMap()
        assertEquals(2, map.stations[0].occupancyLevel)
        assertEquals(PredictionType.LIMITED, map.stations[0].predictionType)
    }

    @Test
    fun `getOccupancyMap defaults to level 1 when no predictions exist`() {
        `when`(stationRepository.findAll()).thenReturn(listOf(s1))
        `when`(predictionRepository.findLatestByType(PredictionType.COMPLETE)).thenReturn(emptyList())
        `when`(predictionRepository.findLatestByType(PredictionType.LIMITED)).thenReturn(emptyList())
        val map = occupancyMapService.getOccupancyMap()
        assertEquals(1, map.stations[0].occupancyLevel)
        assertEquals(PredictionType.LIMITED, map.stations[0].predictionType)
    }

    @Test
    fun `getOccupancyMap returns all stations`() {
        `when`(stationRepository.findAll()).thenReturn(listOf(s1, s2, s3))
        `when`(predictionRepository.findLatestByType(PredictionType.COMPLETE)).thenReturn(emptyList())
        `when`(predictionRepository.findLatestByType(PredictionType.LIMITED)).thenReturn(emptyList())
        val map = occupancyMapService.getOccupancyMap()
        assertEquals(3, map.stations.size)
    }

    @Test
    fun `getOccupancyMap prefers COMPLETE over LIMITED for same station`() {
        `when`(stationRepository.findAll()).thenReturn(listOf(s1))
        `when`(predictionRepository.findLatestByType(PredictionType.COMPLETE)).thenReturn(
            listOf(Prediction(station = s1, user = mock(), occupancyLevel = 5, type = PredictionType.COMPLETE))
        )
        `when`(predictionRepository.findLatestByType(PredictionType.LIMITED)).thenReturn(
            listOf(Prediction(station = s1, user = null, occupancyLevel = 1, type = PredictionType.LIMITED))
        )
        val map = occupancyMapService.getOccupancyMap()
        assertEquals(5, map.stations[0].occupancyLevel)
        assertEquals(PredictionType.COMPLETE, map.stations[0].predictionType)
    }

    @Test
    fun `getOccupancyMap handles mix of stations with and without predictions`() {
        `when`(stationRepository.findAll()).thenReturn(listOf(s1, s2))
        `when`(predictionRepository.findLatestByType(PredictionType.COMPLETE)).thenReturn(
            listOf(Prediction(station = s1, user = mock(), occupancyLevel = 3, type = PredictionType.COMPLETE))
        )
        `when`(predictionRepository.findLatestByType(PredictionType.LIMITED)).thenReturn(emptyList())
        val map = occupancyMapService.getOccupancyMap()
        assertEquals(3, map.stations.find { it.stationId == "s1" }?.occupancyLevel)
        assertEquals(1, map.stations.find { it.stationId == "s2" }?.occupancyLevel)
    }

    @Test
    fun `getOccupancyMap returns correct transport types`() {
        `when`(stationRepository.findAll()).thenReturn(listOf(s1, s2))
        `when`(predictionRepository.findLatestByType(PredictionType.COMPLETE)).thenReturn(emptyList())
        `when`(predictionRepository.findLatestByType(PredictionType.LIMITED)).thenReturn(emptyList())
        val map = occupancyMapService.getOccupancyMap()
        assertEquals(TransportType.METRO, map.stations.find { it.stationId == "s1" }?.transportType)
        assertEquals(TransportType.TRAIN, map.stations.find { it.stationId == "s2" }?.transportType)
    }
}
