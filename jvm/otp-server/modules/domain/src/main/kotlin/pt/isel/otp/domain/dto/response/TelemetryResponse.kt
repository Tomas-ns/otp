package pt.isel.otp.domain.dto.response

import pt.isel.otp.domain.enums.PredictionType

data class TelemetryResponse(
    val stationId: String,
    val stationName: String,
    val occupancyLevel: Int,
    val predictionType: PredictionType,
)
