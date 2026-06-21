package pt.isel.otp.service

import pt.isel.otp.domain.entity.Station

interface LocationService {
    fun findNearest(latitude: Double, longitude: Double): Station
}
