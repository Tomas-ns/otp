package pt.isel.otp.service.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import pt.isel.otp.domain.dto.request.TelemetryRequest
import pt.isel.otp.domain.entity.Station
import pt.isel.otp.domain.enums.TransportType
import java.io.File
import java.io.FileInputStream

@ExtendWith(MockitoExtension::class)
class InferenceServiceImplTest {
    @Mock
    private lateinit var resourceLoader: ResourceLoader

    @Test
    fun `fallback prediction when model not loaded`() {
        val completeResource = mock(Resource::class.java)
        `when`(completeResource.exists()).thenReturn(false)
        val limitedResource = mock(Resource::class.java)
        `when`(limitedResource.exists()).thenReturn(false)
        `when`(resourceLoader.getResource("classpath:models/complete.model")).thenReturn(completeResource)
        `when`(resourceLoader.getResource("classpath:models/limited.model")).thenReturn(limitedResource)
        val service = InferenceServiceImpl(resourceLoader)
        service.init()
        val station = Station("test", "Test", 38.7, -9.1, TransportType.METRO)
        val request = TelemetryRequest(
            timestamp = 1717776000, latitude = 38.7, longitude = -9.1,
            bluetoothCount = 5, bluetoothSignals = listOf(1, 2, 3, 4, 5),
            wifiCount = 3, wifiSignals = listOf(1, 2, 3, 4, 5),
            rsrp = -80, rssnr = 15, rsrq = -10, latencyAvg = 50.0, latencyStdDev = 10.0, packetLoss = 0.5,
        )
        assertEquals(3, service.predictComplete(request, station, java.util.UUID.randomUUID()))
        assertEquals(3, service.predictLimited(station, 1717776000))
    }

    @Test
    fun `models loaded successfully from classpath`() {
        val completeResource = mock(Resource::class.java)
        `when`(completeResource.exists()).thenReturn(true)
        `when`(completeResource.inputStream).thenReturn(FileInputStream("src/test/resources/models/complete.model"))
        val limitedResource = mock(Resource::class.java)
        `when`(limitedResource.exists()).thenReturn(true)
        `when`(limitedResource.inputStream).thenReturn(FileInputStream("src/test/resources/models/limited.model"))
        `when`(resourceLoader.getResource("classpath:models/complete.model")).thenReturn(completeResource)
        `when`(resourceLoader.getResource("classpath:models/limited.model")).thenReturn(limitedResource)
        val service = InferenceServiceImpl(resourceLoader)
        service.init()
        val station = Station("test", "Test", 38.7371, -9.1329, TransportType.METRO)
        val request = TelemetryRequest(
            timestamp = System.currentTimeMillis(), latitude = 38.7371, longitude = -9.1329,
            bluetoothCount = 50, bluetoothSignals = listOf(-60, -65, -70, -72, -75),
            wifiCount = 10, wifiSignals = listOf(-55, -60, -65, -68, -70),
            rsrp = -95, rssnr = 20, rsrq = -8, latencyAvg = 100.0, latencyStdDev = 50.0, packetLoss = 0.0,
        )
        val completeResult = service.predictComplete(request, station, java.util.UUID.randomUUID())
        assertTrue(completeResult in 1..5) { "COMPLETE predicted $completeResult, expected 1..5" }
        val limitedResult = service.predictLimited(station, System.currentTimeMillis())
        assertTrue(limitedResult in 1..5) { "LIMITED predicted $limitedResult, expected 1..5" }
    }

    @Test
    fun `models loaded from models directory`() {
        val modelsDir = File("src/test/resources/models")
        assertTrue(modelsDir.exists(), "Models directory exists")
        val completeFile = File(modelsDir, "complete.model")
        assertTrue(completeFile.exists(), "COMPLETE model file exists")
        val limitedFile = File(modelsDir, "limited.model")
        assertTrue(limitedFile.exists(), "LIMITED model file exists")
    }
}
