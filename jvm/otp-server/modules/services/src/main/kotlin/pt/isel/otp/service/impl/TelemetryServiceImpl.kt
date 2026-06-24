package pt.isel.otp.service.impl

import org.springframework.stereotype.Service
import pt.isel.otp.domain.dto.request.TelemetryRequest
import pt.isel.otp.domain.dto.response.TelemetryResponse
import pt.isel.otp.domain.entity.Prediction
import pt.isel.otp.domain.enums.PredictionType
import pt.isel.otp.repository.PredictionRepository
import pt.isel.otp.repository.UserRepository
import pt.isel.otp.service.InferenceService
import pt.isel.otp.service.LocationService
import pt.isel.otp.service.TelemetryService
import java.util.UUID

@Service
class TelemetryServiceImpl(
    private val locationService: LocationService,
    private val inferenceService: InferenceService,
    private val predictionRepository: PredictionRepository,
    private val userRepository: UserRepository,
) : TelemetryService {
    override fun ingest(request: TelemetryRequest, userId: UUID): TelemetryResponse {
        val station = locationService.findNearest(request.latitude, request.longitude)
        val occupancyLevel = inferenceService.predictComplete(request, station, userId).toShort()
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalStateException("User not found") }

        val prediction = Prediction(
            station = station,
            user = user,
            occupancyLevel = occupancyLevel,
            type = PredictionType.COMPLETE,
        )
        predictionRepository.save(prediction)

        return TelemetryResponse(
            stationId = station.id,
            stationName = station.name,
            occupancyLevel = occupancyLevel.toInt(),
            predictionType = PredictionType.COMPLETE,
        )
    }
}
