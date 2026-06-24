package pt.isel.otp.service

import pt.isel.otp.domain.dto.request.TelemetryRequest
import pt.isel.otp.domain.entity.Station

interface InferenceService {
    fun predictComplete(request: TelemetryRequest, station: Station): Int
    fun predictLimited(station: Station, timestamp: Long): Int
}
