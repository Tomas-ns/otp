package pt.isel.domain

data class StationOccupancyResponse(
    val stationId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val transportType: TransportType,
    val occupancyLevel: Int,
    val predictionType: PredictionType
)