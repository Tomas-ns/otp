package pt.isel.otp.service

import pt.isel.otp.domain.dto.response.OccupancyMapResponse

interface OccupancyMapService {
    fun getOccupancyMap(): OccupancyMapResponse
}
