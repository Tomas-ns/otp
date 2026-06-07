package pt.isel.otp.domain.dto.response

import pt.isel.otp.domain.enums.PredictionType
import pt.isel.otp.domain.enums.TransportType

data class StationOccupancyResponse(
    val stationId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val transportType: TransportType,
    val occupancyLevel: Int,
    val predictionType: PredictionType,
)
