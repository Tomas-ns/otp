package pt.isel.otp.http.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.otp.domain.dto.request.TelemetryRequest
import pt.isel.otp.domain.dto.response.TelemetryResponse
import pt.isel.otp.service.TelemetryService

@RestController
@RequestMapping("/api/v1/telemetry")
class TelemetryController(
    private val telemetryService: TelemetryService,
) {
    @PostMapping
    fun ingest(
        @Valid @RequestBody request: TelemetryRequest,
    ): ResponseEntity<TelemetryResponse> {
        val response = telemetryService.ingest(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}
