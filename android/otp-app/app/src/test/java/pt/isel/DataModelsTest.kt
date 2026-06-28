package pt.isel

import com.google.firebase.firestore.GeoPoint
import org.junit.Assert.*
import org.junit.Test
import pt.isel.datascan.domain.ScanReading
import pt.isel.datascan.domain.TripData
import pt.isel.services.CellularService
import pt.isel.services.NetworkService
import java.util.Date

class DataModelsTest {

    // ---------- ScanReading ----------
    @Test
    fun scanReading_shouldUseDefaultValues() {
        val r = ScanReading()
        assertEquals("", r.userId)
        assertEquals(0, r.bluetoothCount)
        assertEquals(0, r.wifiCount)
        assertTrue(r.signalIntensitiesBT.isEmpty())
        assertTrue(r.signalIntensitiesWF.isEmpty())
        assertNull(r.latitude)
        assertNull(r.longitude)
        assertEquals(0.0, r.latencyAvg, 0.0)
        assertEquals(0.0, r.latencyStdDev, 0.0)
        assertEquals(0.0, r.packetLoss, 0.0)
        assertEquals(0, r.subjectiveRating)
        assertNull(r.rsrp)
        assertNull(r.rssnr)
        assertNull(r.rsrq)
    }

    @Test
    fun scanReading_shouldSetCustomValues() {
        val r = ScanReading(
            userId = "user1",
            timestamp = 1000L,
            bluetoothCount = 5,
            wifiCount = 3,
            signalIntensitiesBT = listOf(-50, -60),
            signalIntensitiesWF = listOf(-70),
            latitude = 38.7,
            longitude = -9.1,
            latencyAvg = 45.8,
            latencyStdDev = 8.9,
            packetLoss = 0.1,
            subjectiveRating = 4,
            rsrp = -96,
            rssnr = 25,
            rsrq = -9
        )
        assertEquals("user1", r.userId)
        assertEquals(1000L, r.timestamp)
        assertEquals(5, r.bluetoothCount)
        assertEquals(3, r.wifiCount)
        assertEquals(listOf(-50, -60), r.signalIntensitiesBT)
        assertEquals(listOf(-70), r.signalIntensitiesWF)
        assertEquals(38.7, r.latitude!!, 0.001)
        assertEquals(-9.1, r.longitude!!, 0.001)
        assertEquals(45.8, r.latencyAvg, 0.001)
        assertEquals(8.9, r.latencyStdDev, 0.001)
        assertEquals(0.1, r.packetLoss, 0.001)
        assertEquals(4, r.subjectiveRating)
        assertEquals(-96, r.rsrp!!.toInt())
        assertEquals(25, r.rssnr!!.toInt())
        assertEquals(-9, r.rsrq!!.toInt())
    }

    @Test
    fun scanReading_toMap_shouldContainAllKeys() {
        val r = ScanReading(userId = "u1", bluetoothCount = 2, wifiCount = 1, latitude = 38.7, longitude = -9.1)
        val map = r.toMap()
        assertEquals("u1", map["userId"] as String)
        assertEquals(2, map["bluetoothCount"] as Int)
        assertEquals(1, map["wifiCount"] as Int)
        assertNotNull(map["location"])
        assertTrue(map["location"] is GeoPoint)
        val loc = map["location"] as GeoPoint
        assertEquals(38.7, loc.latitude, 0.001)
        assertEquals(-9.1, loc.longitude, 0.001)
    }

    @Test
    fun scanReading_toMap_shouldHandleNullLocation() {
        val r = ScanReading(userId = "u1")
        val map = r.toMap()
        val loc = map["location"] as GeoPoint
        assertEquals(0.0, loc.latitude, 0.0)
        assertEquals(0.0, loc.longitude, 0.0)
    }

    @Test
    fun scanReading_toMap_shouldIncludeAllMetrics() {
        val r = ScanReading(
            latencyAvg = 10.5, latencyStdDev = 2.1, packetLoss = 0.05,
            subjectiveRating = 3, rsrp = -100, rssnr = 20, rsrq = -10
        )
        val map = r.toMap()
        assertEquals(10.5, map["latencyAvg"] as Double, 0.001)
        assertEquals(2.1, map["latencyStdDev"] as Double, 0.001)
        assertEquals(0.05, map["packetLoss"] as Double, 0.001)
        assertEquals(3, map["subjectiveRating"] as Int)
        assertEquals(-100, map["rsrp"] as Int)
        assertEquals(20, map["rssnr"] as Int)
        assertEquals(-10, map["rsrq"] as Int)
    }

    // ---------- TripData ----------
    @Test
    fun tripData_shouldCreateWithAllFields() {
        val date = Date()
        val t = TripData("METRO", date, true)
        assertEquals("METRO", t.transportType)
        assertEquals(date, t.startDate)
        assertTrue(t.isTripValid)
    }

    @Test
    fun tripData_isTripValidDefaultsToTrue() {
        val t = TripData("TRAIN", Date())
        assertTrue(t.isTripValid)
    }

    @Test
    fun tripData_invalidTrip() {
        val t = TripData("METRO", Date(), false)
        assertFalse(t.isTripValid)
    }

    @Test
    fun tripData_toMap_shouldContainAllKeys() {
        val date = Date()
        val t = TripData("BUS", date, false)
        val map = t.toMap()
        assertEquals("BUS", map["transportType"] as String)
        assertEquals(date, map["startDate"] as Date)
        assertEquals(false, map["isTripValid"] as Boolean)
    }

    @Test
    fun tripData_toMap_validTrip() {
        val date = Date()
        val t = TripData("TRAIN", date)
        val map = t.toMap()
        assertEquals("TRAIN", map["transportType"] as String)
        assertEquals(date, map["startDate"] as Date)
        assertEquals(true, map["isTripValid"] as Boolean)
    }

    // ---------- CellularMetrics ----------
    @Test
    fun cellularMetrics_creation() {
        val m = CellularService.CellularMetrics()
        assertNull(m.rsrp)
        assertNull(m.rssnr)
        assertNull(m.rsrq)
    }

    @Test
    fun cellularMetrics_withValues() {
        val m = CellularService.CellularMetrics(rsrp = -96, rssnr = 25, rsrq = -9)
        assertEquals(-96, m.rsrp!!.toInt())
        assertEquals(25, m.rssnr!!.toInt())
        assertEquals(-9, m.rsrq!!.toInt())
    }

    @Test
    fun cellularMetrics_partialValues() {
        val m = CellularService.CellularMetrics(rsrp = -110)
        assertEquals(-110, m.rsrp!!.toInt())
        assertNull(m.rssnr)
        assertNull(m.rsrq)
    }

    // ---------- NetworkMetrics ----------
    @Test
    fun networkMetrics_creation() {
        val m = NetworkService.NetworkMetrics(0.0, 0.0, 0.0)
        assertEquals(0.0, m.latencyAvg, 0.0)
        assertEquals(0.0, m.latencyStdDev, 0.0)
        assertEquals(0.0, m.packetLoss, 0.0)
    }

    @Test
    fun networkMetrics_withValues() {
        val m = NetworkService.NetworkMetrics(45.8, 8.9, 0.1)
        assertEquals(45.8, m.latencyAvg, 0.001)
        assertEquals(8.9, m.latencyStdDev, 0.001)
        assertEquals(0.1, m.packetLoss, 0.001)
    }

    @Test
    fun networkMetrics_highValues() {
        val m = NetworkService.NetworkMetrics(500.0, 100.0, 0.8)
        assertEquals(500.0, m.latencyAvg, 0.001)
        assertEquals(100.0, m.latencyStdDev, 0.001)
        assertEquals(0.8, m.packetLoss, 0.001)
    }
}
