package pt.isel.otp.service.impl

import org.springframework.stereotype.Service
import pt.isel.otp.domain.dto.request.TelemetryRequest
import pt.isel.otp.domain.dto.response.TelemetryResponse
import pt.isel.otp.service.TelemetryService

@Service
class TelemetryServiceImpl : TelemetryService {
    override fun ingest(request: TelemetryRequest): TelemetryResponse {
        throw UnsupportedOperationException("Telemetry ingestion not yet implemented")
    }
}
