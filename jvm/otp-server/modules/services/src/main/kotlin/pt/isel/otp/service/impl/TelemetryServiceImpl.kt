package pt.isel.otp.service.impl

import org.springframework.stereotype.Service
import pt.isel.otp.domain.dto.request.TelemetryRequest
import pt.isel.otp.domain.dto.response.TelemetryResponse
import pt.isel.otp.domain.entity.Prediction
import pt.isel.otp.domain.enums.PredictionType
import pt.isel.otp.repository.PredictionRepository
import pt.isel.otp.repository.UserRepository
import pt.isel.otp.service.LocationService
import pt.isel.otp.service.TelemetryService
import java.util.UUID
import kotlin.random.Random

@Service
class TelemetryServiceImpl(
    private val locationService: LocationService,
    private val predictionRepository: PredictionRepository,
    private val userRepository: UserRepository,
) : TelemetryService {
    override fun ingest(request: TelemetryRequest): TelemetryResponse {
        val station = locationService.findNearest(request.latitude, request.longitude)
        val occupancyLevel = Random.nextInt(1, 6).toShort()
        val testUser = userRepository.findById(UUID.fromString("a0000000-0000-0000-0000-000000000001"))
            .orElseThrow { IllegalStateException("Test user not found") }

        val prediction = Prediction(
            station = station,
            user = testUser,
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
