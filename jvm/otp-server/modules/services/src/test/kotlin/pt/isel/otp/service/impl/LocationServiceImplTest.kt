package pt.isel.otp.service.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import pt.isel.otp.domain.entity.Station
import pt.isel.otp.domain.enums.TransportType
import pt.isel.otp.repository.StationRepository

@ExtendWith(MockitoExtension::class)
class LocationServiceImplTest {
    @Mock
    private lateinit var stationRepository: StationRepository

    private lateinit var locationService: LocationServiceImpl

    private val lisbonMetro = Station("lisbon", "Lisbon", 38.7223, -9.1393, TransportType.METRO)
    private val portoMetro = Station("porto", "Porto", 41.1579, -8.6291, TransportType.METRO)
    private val alameda = Station("alameda", "Alameda", 38.7371, -9.1329, TransportType.METRO)
    private val orinte = Station("oriente", "Oriente", 38.7680, -9.0997, TransportType.METRO)
    private val cascais = Station("cascais", "Cascais", 38.7009, -9.4179, TransportType.TRAIN)

    @BeforeEach
    fun setup() {
        locationService = LocationServiceImpl(stationRepository)
    }

    @Test
    fun `findNearest returns station closest to given coordinates`() {
        `when`(stationRepository.findAll()).thenReturn(listOf(lisbonMetro, portoMetro))
        val nearest = locationService.findNearest(38.72, -9.14)
        assertEquals("lisbon", nearest.id)
    }

    @Test
    fun `findNearest with alameda coordinates returns alameda`() {
        `when`(stationRepository.findAll()).thenReturn(listOf(alameda, orinte, cascais))
        val nearest = locationService.findNearest(38.7371, -9.1329)
        assertEquals("alameda", nearest.id)
    }

    @Test
    fun `findNearest with oriente coordinates returns oriente`() {
        `when`(stationRepository.findAll()).thenReturn(listOf(alameda, orinte, cascais))
        val nearest = locationService.findNearest(38.768, -9.0997)
        assertEquals("oriente", nearest.id)
    }

    @Test
    fun `findNearest with cascais coordinates returns cascais`() {
        `when`(stationRepository.findAll()).thenReturn(listOf(alameda, orinte, cascais))
        val nearest = locationService.findNearest(38.7009, -9.4179)
        assertEquals("cascais", nearest.id)
    }

    @Test
    fun `findNearest with coordinates equidistant between two stations picks first`() {
        `when`(stationRepository.findAll()).thenReturn(listOf(alameda, orinte))
        val nearest = locationService.findNearest(38.75, -9.116)
        assertNotNull(nearest)
    }

    @Test
    fun `findNearest returns only station when only one exists`() {
        `when`(stationRepository.findAll()).thenReturn(listOf(lisbonMetro))
        val nearest = locationService.findNearest(40.0, -8.0)
        assertEquals("lisbon", nearest.id)
    }

    @Test
    fun `findNearest with same coordinates returns exact station`() {
        `when`(stationRepository.findAll()).thenReturn(listOf(alameda, orinte, cascais))
        val nearest = locationService.findNearest(alameda.latitude, alameda.longitude)
        assertEquals("alameda", nearest.id)
    }

    @Test
    fun `findNearest works with TRAIN stations`() {
        `when`(stationRepository.findAll()).thenReturn(listOf(cascais, lisbonMetro))
        val nearest = locationService.findNearest(38.7, -9.4)
        assertEquals("cascais", nearest.id)
    }
}
