package pt.isel.api

import android.util.Log
import pt.isel.domain.StationOccupancyResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.serialization.gson.gson
import pt.isel.domain.AuthResponse
import pt.isel.domain.OccupancyMapResponse

data class OccupancyResponse(
    val station: String,
    val occupancyLevel: String
)

class ApiAccess {
    private var cachedToken: String? = null
    private var expiryTime: Long = 0
    private val baseUrl = "http://34.76.146.40:8080"

    private val client = HttpClient(Android) {
        install(ContentNegotiation) { gson() }
    }

    private suspend fun getValidToken(): String {
        val isExpired = System.currentTimeMillis() >= (expiryTime - 60000)

        if (cachedToken == null || isExpired) {
            val authData = fetchNewTokenFromServer()

            cachedToken = authData.accessToken

            expiryTime = System.currentTimeMillis() + (15 * 60 * 1000)
        }
        return cachedToken!!
    }

    suspend fun fetchNewTokenFromServer(): AuthResponse {
        return try {
            client.post("$baseUrl/api/v1/auth/token").body()
        } catch (e: Exception) {
            Log.e("API_DEBUG", "Falha ao renovar token: ${e.message}")
            throw e
        }
    }

    suspend fun fetchStationOccupancy(stationId: String, time : Long): Int {
        getValidToken()
        val response = client.get("$baseUrl/api/v1/occupancy/stations/$stationId") {
            parameter("timestamp", time)
            header("Authorization", "Bearer $cachedToken")
        }
        return response.body<StationOccupancyResponse>().occupancyLevel
    }

    suspend fun fetchCompletePrediction() : OccupancyMapResponse{
        getValidToken()
        Log.e("API_DEBUG", "Antes de ver previsoes")
        val response = client.get("$baseUrl/api/v1/occupancy/map") {
            header("Authorization", "Bearer $cachedToken")
        }
        Log.e("API_DEBUG", "Depois de ver previsoes $response")
        return response.body<OccupancyMapResponse>()
    }
}