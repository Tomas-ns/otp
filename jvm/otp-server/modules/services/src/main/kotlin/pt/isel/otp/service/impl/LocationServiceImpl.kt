package pt.isel.otp.service.impl

import org.springframework.stereotype.Service
import pt.isel.otp.domain.entity.Station
import pt.isel.otp.repository.StationRepository
import pt.isel.otp.service.LocationService
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Service
class LocationServiceImpl(
    private val stationRepository: StationRepository,
) : LocationService {
    override fun findNearest(latitude: Double, longitude: Double): Station {
        val stations = stationRepository.findAll()
        return stations.minBy { haversine(latitude, longitude, it.latitude, it.longitude) }
    }

    private fun haversine(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double,
    ): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    private fun Double.pow(exp: Int): Double = this * this
}
