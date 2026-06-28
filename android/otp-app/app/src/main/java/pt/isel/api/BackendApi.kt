package pt.isel.api

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class TelemetryRequest(
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val bluetoothCount: Int,
    val bluetoothSignals: List<Int>,
    val wifiCount: Int,
    val wifiSignals: List<Int>,
    val rsrp: Int,
    val rssnr: Int,
    val rsrq: Int,
    val latencyAvg: Double,
    val latencyStdDev: Double,
    val packetLoss: Double,
)

data class TelemetryResponse(
    val stationId: String? = null,
    val stationName: String? = null,
    val occupancyLevel: Int? = null,
    val predictionType: String? = null,
)

data class AuthResponse(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val user: UserResponse? = null,
)

data class UserResponse(
    val id: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
)

object BackendApi {
    private var accessToken: String? = null
    private var refreshToken: String? = null

    private val client by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) {
                gson()
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 15000
                connectTimeoutMillis = 5000
            }
        }
    }

    suspend fun authenticate(): Boolean = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = client.post("${BackendConfig.baseUrl}/api/v1/auth/token") {
                contentType(ContentType.Application.Json)
            }
            if (response.status.isSuccess()) {
                val authResponse = response.body<AuthResponse>()
                accessToken = authResponse.accessToken
                refreshToken = authResponse.refreshToken
                Log.d("BackendApi", "Authenticated successfully, token: ${accessToken?.take(20)}...")
                true
            } else {
                Log.w("BackendApi", "Auth failed: ${response.status}")
                false
            }
        } catch (e: Exception) {
            Log.e("BackendApi", "Auth error: ${e.message}", e)
            false
        }
    }

    suspend fun sendTelemetry(request: TelemetryRequest): TelemetryResponse? = withContext(Dispatchers.IO) {
        try {
            var token = accessToken
            if (token == null) {
                Log.w("BackendApi", "No token, attempting re-auth")
                if (!authenticate()) return@withContext null
                token = accessToken
            }

            val response: HttpResponse = client.post("${BackendConfig.baseUrl}/api/v1/telemetry") {
                contentType(ContentType.Application.Json)
                bearerAuth(token!!)
                setBody(request)
            }

            if (response.status.isSuccess()) {
                val telemetryResponse = response.body<TelemetryResponse>()
                Log.d("BackendApi", "Telemetry sent: station=${telemetryResponse.stationName}, occupancy=${telemetryResponse.occupancyLevel}")
                telemetryResponse
            } else if (response.status.value == 401) {
                Log.w("BackendApi", "Token expired, re-authenticating")
                accessToken = null
                if (authenticate()) {
                    return@withContext sendTelemetry(request)
                }
                null
            } else {
                Log.w("BackendApi", "Telemetry failed: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e("BackendApi", "Telemetry error: ${e.message}", e)
            null
        }
    }
}