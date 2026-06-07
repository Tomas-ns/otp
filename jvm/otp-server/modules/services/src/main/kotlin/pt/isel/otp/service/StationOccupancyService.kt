package pt.isel.otp.service

import pt.isel.otp.domain.dto.response.StationOccupancyResponse

interface StationOccupancyService {
    fun getLimitedPrediction(
        stationId: String,
        timestamp: Long,
    ): StationOccupancyResponse
}
