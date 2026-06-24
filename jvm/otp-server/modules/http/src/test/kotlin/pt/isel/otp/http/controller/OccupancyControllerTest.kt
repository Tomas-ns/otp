package pt.isel.otp.http.controller

import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import pt.isel.otp.domain.dto.response.OccupancyMapResponse
import pt.isel.otp.domain.dto.response.StationOccupancyResponse
import pt.isel.otp.domain.enums.PredictionType
import pt.isel.otp.domain.enums.TransportType
import pt.isel.otp.service.OccupancyMapService
import pt.isel.otp.service.StationOccupancyService

@WebMvcTest(
    controllers = [OccupancyController::class],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class],
)
class OccupancyControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var occupancyMapService: OccupancyMapService

    @MockitoBean
    private lateinit var stationOccupancyService: StationOccupancyService

    @Test
    fun `GET map returns 200 with stations`() {
        val stations = listOf(
            StationOccupancyResponse("s1", "S1", 1.0, 2.0, TransportType.METRO, 3, PredictionType.COMPLETE),
            StationOccupancyResponse("s2", "S2", 3.0, 4.0, TransportType.TRAIN, 1, PredictionType.LIMITED),
        )
        `when`(occupancyMapService.getOccupancyMap()).thenReturn(OccupancyMapResponse(stations))
        mockMvc.perform(get("/api/v1/occupancy/map"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.stations.length()").value(2))
            .andExpect(jsonPath("$.stations[0].stationId").value("s1"))
            .andExpect(jsonPath("$.stations[1].stationId").value("s2"))
    }

    @Test
    fun `GET map returns empty stations`() {
        `when`(occupancyMapService.getOccupancyMap()).thenReturn(OccupancyMapResponse(emptyList()))
        mockMvc.perform(get("/api/v1/occupancy/map"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.stations.length()").value(0))
    }

    @Test
    fun `GET station occupancy returns 200`() {
        val response = StationOccupancyResponse("s1", "S1", 1.0, 2.0, TransportType.METRO, 4, PredictionType.LIMITED)
        `when`(stationOccupancyService.getLimitedPrediction("s1", 1717776000)).thenReturn(response)
        mockMvc.perform(
            get("/api/v1/occupancy/stations/s1")
                .param("timestamp", "1717776000")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.stationId").value("s1"))
            .andExpect(jsonPath("$.occupancyLevel").value(4))
    }

    @Test
    fun `GET station occupancy with different station`() {
        val response = StationOccupancyResponse("s2", "S2", 3.0, 4.0, TransportType.TRAIN, 2, PredictionType.LIMITED)
        `when`(stationOccupancyService.getLimitedPrediction("s2", 1717776000)).thenReturn(response)
        mockMvc.perform(
            get("/api/v1/occupancy/stations/s2")
                .param("timestamp", "1717776000")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.stationId").value("s2"))
            .andExpect(jsonPath("$.transportType").value("TRAIN"))
    }

    @Test
    fun `GET station occupancy returns 400 when timestamp missing`() {
        mockMvc.perform(get("/api/v1/occupancy/stations/s1"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `GET station occupancy returns 400 when timestamp negative`() {
        mockMvc.perform(
            get("/api/v1/occupancy/stations/s1")
                .param("timestamp", "-1")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `GET map returns correct data types`() {
        val station = StationOccupancyResponse("s1", "S1", 38.7, -9.1, TransportType.METRO, 5, PredictionType.COMPLETE)
        `when`(occupancyMapService.getOccupancyMap()).thenReturn(OccupancyMapResponse(listOf(station)))
        mockMvc.perform(get("/api/v1/occupancy/map"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.stations[0].latitude").value(38.7))
            .andExpect(jsonPath("$.stations[0].longitude").value(-9.1))
            .andExpect(jsonPath("$.stations[0].occupancyLevel").value(5))
            .andExpect(jsonPath("$.stations[0].predictionType").value("COMPLETE"))
    }
}
