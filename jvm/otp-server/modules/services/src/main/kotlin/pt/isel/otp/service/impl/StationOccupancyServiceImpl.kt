package pt.isel.otp.service.impl

import org.springframework.stereotype.Service
import pt.isel.otp.domain.dto.response.StationOccupancyResponse
import pt.isel.otp.domain.enums.PredictionType
import pt.isel.otp.repository.StationRepository
import pt.isel.otp.service.InferenceService
import pt.isel.otp.service.StationOccupancyService

@Service
class StationOccupancyServiceImpl(
    private val stationRepository: StationRepository,
    private val inferenceService: InferenceService,
) : StationOccupancyService {
    override fun getLimitedPrediction(
        stationId: String,
        timestamp: Long,
    ): StationOccupancyResponse {
        val station = stationRepository.findById(stationId)
            .orElseThrow { NoSuchElementException("Station not found: $stationId") }

        val occupancyLevel = inferenceService.predictLimited(station, timestamp).toShort()

        return StationOccupancyResponse(
            stationId = station.id,
            name = station.name,
            latitude = station.latitude,
            longitude = station.longitude,
            transportType = station.transportType,
            occupancyLevel = occupancyLevel.toInt(),
            predictionType = PredictionType.LIMITED,
        )
    }
}
