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
import pt.isel.otp.domain.dto.response.TelemetryResponse
import pt.isel.otp.domain.enums.PredictionType
import pt.isel.otp.service.TelemetryService
import java.util.UUID

@WebMvcTest(
    controllers = [TelemetryController::class],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class],
)
class TelemetryControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var telemetryService: TelemetryService

    @Test
    fun `POST telemetry returns 201 with valid request and auth`() {
        val response = TelemetryResponse(stationId = "s1", stationName = "S1", occupancyLevel = 4, predictionType = PredictionType.COMPLETE)
        `when`(telemetryService.ingest(any(), any())).thenReturn(response)
        mockMvc.perform(
            post("/api/v1/telemetry")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test-token")
                .content("""{"timestamp":1717776000,"latitude":38.7,"longitude":-9.1,"bluetoothCount":5,"bluetoothSignals":[1,2,3,4,5],"wifiCount":3,"wifiSignals":[1,2,3,4,5],"rsrp":-80,"rssnr":15,"rsrq":-10,"latencyAvg":50.0,"latencyStdDev":10.0,"packetLoss":0.5}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.stationId").value("s1"))
            .andExpect(jsonPath("$.occupancyLevel").value(4))
    }

    @Test
    fun `POST telemetry returns 400 when bluetoothSignals too short`() {
        mockMvc.perform(
            post("/api/v1/telemetry")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test-token")
                .content("""{"timestamp":1717776000,"latitude":38.7,"longitude":-9.1,"bluetoothCount":1,"bluetoothSignals":[1],"wifiCount":1,"wifiSignals":[1,2,3,4,5],"rsrp":-80,"rssnr":15,"rsrq":-10,"latencyAvg":50.0,"latencyStdDev":10.0,"packetLoss":0.5}""")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST telemetry returns 400 when wifiSignals too short`() {
        mockMvc.perform(
            post("/api/v1/telemetry")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test-token")
                .content("""{"timestamp":1717776000,"latitude":38.7,"longitude":-9.1,"bluetoothCount":1,"bluetoothSignals":[1,2,3,4,5],"wifiCount":1,"wifiSignals":[1],"rsrp":-80,"rssnr":15,"rsrq":-10,"latencyAvg":50.0,"latencyStdDev":10.0,"packetLoss":0.5}""")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST telemetry returns 400 when packetLoss exceeds 100`() {
        mockMvc.perform(
            post("/api/v1/telemetry")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test-token")
                .content("""{"timestamp":1717776000,"latitude":38.7,"longitude":-9.1,"bluetoothCount":5,"bluetoothSignals":[1,2,3,4,5],"wifiCount":3,"wifiSignals":[1,2,3,4,5],"rsrp":-80,"rssnr":15,"rsrq":-10,"latencyAvg":50.0,"latencyStdDev":10.0,"packetLoss":150.0}""")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST telemetry returns 400 when timestamp is negative`() {
        mockMvc.perform(
            post("/api/v1/telemetry")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test-token")
                .content("""{"timestamp":-1,"latitude":38.7,"longitude":-9.1,"bluetoothCount":5,"bluetoothSignals":[1,2,3,4,5],"wifiCount":3,"wifiSignals":[1,2,3,4,5],"rsrp":-80,"rssnr":15,"rsrq":-10,"latencyAvg":50.0,"latencyStdDev":10.0,"packetLoss":0.5}""")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST telemetry returns 400 when latencyAvg is negative`() {
        mockMvc.perform(
            post("/api/v1/telemetry")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test-token")
                .content("""{"timestamp":1717776000,"latitude":38.7,"longitude":-9.1,"bluetoothCount":5,"bluetoothSignals":[1,2,3,4,5],"wifiCount":3,"wifiSignals":[1,2,3,4,5],"rsrp":-80,"rssnr":15,"rsrq":-10,"latencyAvg":-1.0,"latencyStdDev":10.0,"packetLoss":0.5}""")
        )
            .andExpect(status().isBadRequest)
    }
}
