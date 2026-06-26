package pt.isel.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.gson.gson

data class OccupancyResponse(
    val station: String,
    val occupancyLevel: String
)

class ApiAccess {
    private val baseUrl = "http://10.252.140.115:8080/api/v1/occupancy/stations"

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            gson()
        }
    }

    suspend fun fetchStationOccupancy(stationName: String): String {
        val response = client.get(baseUrl) {
            parameter("stationName", stationName)
        }

        return response.body<String>()
    }
}