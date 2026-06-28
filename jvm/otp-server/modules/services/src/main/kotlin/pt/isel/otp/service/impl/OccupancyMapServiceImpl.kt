package pt.isel.otp.service.impl

import org.springframework.stereotype.Service
import pt.isel.otp.domain.dto.response.OccupancyMapResponse
import pt.isel.otp.domain.dto.response.StationOccupancyResponse
import pt.isel.otp.domain.enums.PredictionType
import pt.isel.otp.repository.PredictionRepository
import pt.isel.otp.repository.StationRepository
import pt.isel.otp.service.OccupancyMapService

@Service
class OccupancyMapServiceImpl(
    private val predictionRepository: PredictionRepository,
    private val stationRepository: StationRepository,
) : OccupancyMapService {
    override fun getOccupancyMap(): OccupancyMapResponse {
        val stations = stationRepository.findAll()
            .associateBy { it.id }

        val stationResponses = predictionRepository
            .findLatestByType(PredictionType.COMPLETE)
            .mapNotNull { prediction ->
                val station = stations[prediction.station.id] ?: return@mapNotNull null
                StationOccupancyResponse(
                    stationId = station.id,
                    name = station.name,
                    latitude = station.latitude,
                    longitude = station.longitude,
                    transportType = station.transportType,
                    occupancyLevel = prediction.occupancyLevel.toInt(),
                    predictionType = prediction.type,
                )
            }

        return OccupancyMapResponse(stations = stationResponses)
    }
}
