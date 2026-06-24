package pt.isel.otp.service

import pt.isel.otp.domain.dto.request.TelemetryRequest
import pt.isel.otp.domain.entity.Station
import java.util.UUID

interface InferenceService {
    fun predictComplete(request: TelemetryRequest, station: Station, userId: UUID): Int
    fun predictLimited(station: Station, timestamp: Long): Int
}
