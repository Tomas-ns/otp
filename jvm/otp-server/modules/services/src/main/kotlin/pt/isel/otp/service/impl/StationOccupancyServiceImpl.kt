package pt.isel.otp.service.impl

import org.springframework.stereotype.Service
import pt.isel.otp.domain.dto.response.StationOccupancyResponse
import pt.isel.otp.domain.entity.Prediction
import pt.isel.otp.domain.enums.PredictionType
import pt.isel.otp.repository.PredictionRepository
import pt.isel.otp.repository.StationRepository
import pt.isel.otp.service.StationOccupancyService
import kotlin.random.Random

@Service
class StationOccupancyServiceImpl(
    private val stationRepository: StationRepository,
    private val predictionRepository: PredictionRepository,
) : StationOccupancyService {
    override fun getLimitedPrediction(
        stationId: String,
        timestamp: Long,
    ): StationOccupancyResponse {
        val station = stationRepository.findById(stationId)
            .orElseThrow { NoSuchElementException("Station not found: $stationId") }

        val occupancyLevel = Random.nextInt(1, 6).toShort()

        val prediction = Prediction(
            station = station,
            user = null,
            occupancyLevel = occupancyLevel,
            type = PredictionType.LIMITED,
        )
        predictionRepository.save(prediction)

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
