package pt.isel.otp.service

import pt.isel.otp.domain.dto.request.TelemetryRequest
import pt.isel.otp.domain.dto.response.TelemetryResponse
import java.util.UUID

interface TelemetryService {
    fun ingest(request: TelemetryRequest, userId: UUID): TelemetryResponse
}
