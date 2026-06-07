package pt.isel.otp.service.impl

import org.springframework.stereotype.Service
import pt.isel.otp.domain.dto.response.StationOccupancyResponse
import pt.isel.otp.service.StationOccupancyService

@Service
class StationOccupancyServiceImpl : StationOccupancyService {
    override fun getLimitedPrediction(
        stationId: String,
        timestamp: Long,
    ): StationOccupancyResponse {
        throw UnsupportedOperationException("Station limited prediction not yet implemented")
    }
}
