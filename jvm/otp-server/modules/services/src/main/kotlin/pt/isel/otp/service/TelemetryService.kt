package pt.isel.otp.service

import pt.isel.otp.domain.dto.request.TelemetryRequest
import pt.isel.otp.domain.dto.response.TelemetryResponse

interface TelemetryService {
    fun ingest(request: TelemetryRequest): TelemetryResponse
}
