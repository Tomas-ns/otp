package pt.isel.otp.http.controller

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.otp.domain.dto.response.OccupancyMapResponse
import pt.isel.otp.domain.dto.response.StationOccupancyResponse
import pt.isel.otp.service.OccupancyMapService
import pt.isel.otp.service.StationOccupancyService

@RestController
@RequestMapping("/api/v1/occupancy")
@Validated
class OccupancyController(
    private val occupancyMapService: OccupancyMapService,
    private val stationOccupancyService: StationOccupancyService,
) {
    @GetMapping("/map")
    fun getMap(): OccupancyMapResponse = occupancyMapService.getOccupancyMap()

    @GetMapping("/stations/{stationId}")
    fun getStationOccupancy(
        @PathVariable @NotBlank stationId: String,
        @RequestParam @PositiveOrZero timestamp: Long,
    ): StationOccupancyResponse =
        stationOccupancyService.getLimitedPrediction(
            stationId = stationId,
            timestamp = timestamp,
        )
}
