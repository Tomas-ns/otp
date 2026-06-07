package pt.isel.otp.service.impl

import org.springframework.stereotype.Service
import pt.isel.otp.domain.dto.response.OccupancyMapResponse
import pt.isel.otp.service.OccupancyMapService

@Service
class OccupancyMapServiceImpl : OccupancyMapService {
    override fun getOccupancyMap(): OccupancyMapResponse {
        throw UnsupportedOperationException("Occupancy map not yet implemented")
    }
}
