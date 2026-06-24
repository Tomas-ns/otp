package pt.isel.otp.service.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import pt.isel.otp.domain.dto.request.TelemetryRequest
import pt.isel.otp.domain.dto.response.TelemetryResponse
import pt.isel.otp.domain.entity.Prediction
import pt.isel.otp.domain.entity.Station
import pt.isel.otp.domain.entity.User
import pt.isel.otp.domain.enums.PredictionType
import pt.isel.otp.domain.enums.TransportType
import pt.isel.otp.repository.PredictionRepository
import pt.isel.otp.repository.UserRepository
import pt.isel.otp.service.InferenceService
import pt.isel.otp.service.LocationService
import java.util.*

@ExtendWith(MockitoExtension::class)
class TelemetryServiceImplTest {
    @Mock private lateinit var locationService: LocationService
    @Mock private lateinit var inferenceService: InferenceService
    @Mock private lateinit var predictionRepository: PredictionRepository
    @Mock private lateinit var userRepository: UserRepository

    private lateinit var telemetryService: TelemetryServiceImpl

    private val userId = UUID.randomUUID()
    private val station = Station("alameda_metro", "Alameda", 38.7371, -9.1329, TransportType.METRO)
    private val user = User(id = userId, email = "u@u.com")

    private val validRequest = TelemetryRequest(
        timestamp = 1717776000, latitude = 38.7371, longitude = -9.1329,
        bluetoothCount = 5, bluetoothSignals = listOf(1, 2, 3, 4, 5),
        wifiCount = 3, wifiSignals = listOf(1, 2, 3, 4, 5),
        rsrp = -80, rssnr = 15, rsrq = -10, latencyAvg = 50.0, latencyStdDev = 10.0, packetLoss = 0.5,
    )

    @BeforeEach
    fun setup() {
        telemetryService = TelemetryServiceImpl(locationService, inferenceService, predictionRepository, userRepository)
    }

    @Test
    fun `ingest saves COMPLETE prediction and returns response`() {
        `when`(locationService.findNearest(38.7371, -9.1329)).thenReturn(station)
        `when`(inferenceService.predictComplete(validRequest, station, userId)).thenReturn(4)
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        val response = telemetryService.ingest(validRequest, userId)
        assertEquals("alameda_metro", response.stationId)
        assertEquals("Alameda", response.stationName)
        assertEquals(4, response.occupancyLevel)
        assertEquals(PredictionType.COMPLETE, response.predictionType)
        verify(predictionRepository).save(any(Prediction::class.java))
    }

    @Test
    fun `ingest calls location service with correct coordinates`() {
        `when`(locationService.findNearest(38.7371, -9.1329)).thenReturn(station)
        `when`(inferenceService.predictComplete(validRequest, station, userId)).thenReturn(3)
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        telemetryService.ingest(validRequest, userId)
        verify(locationService).findNearest(38.7371, -9.1329)
    }

    @Test
    fun `ingest handles occupancy level 1`() {
        `when`(locationService.findNearest(38.7371, -9.1329)).thenReturn(station)
        `when`(inferenceService.predictComplete(validRequest, station, userId)).thenReturn(1)
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        assertEquals(1, telemetryService.ingest(validRequest, userId).occupancyLevel)
    }

    @Test
    fun `ingest handles occupancy level 5`() {
        `when`(locationService.findNearest(38.7371, -9.1329)).thenReturn(station)
        `when`(inferenceService.predictComplete(validRequest, station, userId)).thenReturn(5)
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        assertEquals(5, telemetryService.ingest(validRequest, userId).occupancyLevel)
    }

    @Test
    fun `ingest uses TRAIN station`() {
        val trainStation = Station("cascais_train", "Cascais", 38.7009, -9.4179, TransportType.TRAIN)
        val trainRequest = validRequest.copy(latitude = 38.7009, longitude = -9.4179)
        `when`(locationService.findNearest(38.7009, -9.4179)).thenReturn(trainStation)
        `when`(inferenceService.predictComplete(trainRequest, trainStation, userId)).thenReturn(2)
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        val response = telemetryService.ingest(trainRequest, userId)
        assertEquals("cascais_train", response.stationId)
    }

    @Test
    fun `ingest throws when user not found`() {
        `when`(locationService.findNearest(38.7371, -9.1329)).thenReturn(station)
        `when`(inferenceService.predictComplete(validRequest, station, userId)).thenReturn(3)
        `when`(userRepository.findById(userId)).thenReturn(Optional.empty())
        assertThrows(IllegalStateException::class.java) { telemetryService.ingest(validRequest, userId) }
    }

    @Test
    fun `ingest saves prediction with correct user and type`() {
        `when`(locationService.findNearest(38.7371, -9.1329)).thenReturn(station)
        `when`(inferenceService.predictComplete(validRequest, station, userId)).thenReturn(4)
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        telemetryService.ingest(validRequest, userId)
        verify(predictionRepository).save(argThat { p ->
            p.user?.id == userId && p.type == PredictionType.COMPLETE && p.station.id == station.id
        })
    }


}
