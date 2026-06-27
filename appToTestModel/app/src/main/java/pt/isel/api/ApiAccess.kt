package pt.isel.api

import android.util.Log
import domain.AuthResponse
import domain.StationOccupancyResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.serialization.gson.gson

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

    suspend fun fetchStationOccupancy(stationId: String): Int {
        getValidToken()
        val currentTimestamp = System.currentTimeMillis()
        val response = client.get("$baseUrl/api/v1/occupancy/stations/$stationId") {
            parameter("timestamp", currentTimestamp)
            header("Authorization", "Bearer $cachedToken")
        }
        return response.body<StationOccupancyResponse>().occupancyLevel
    }
}