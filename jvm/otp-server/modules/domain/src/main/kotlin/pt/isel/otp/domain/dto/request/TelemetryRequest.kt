package pt.isel.otp.domain.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size

data class TelemetryRequest(
    @field:NotNull
    @field:PositiveOrZero
    val timestamp: Long,
    @field:NotNull
    val latitude: Double,
    @field:NotNull
    val longitude: Double,
    @field:NotNull
    @field:PositiveOrZero
    val bluetoothCount: Int,
    @field:NotNull
    @field:Size(min = 5, max = 5)
    val bluetoothSignals: List<Int>,
    @field:NotNull
    @field:PositiveOrZero
    val wifiCount: Int,
    @field:NotNull
    @field:Size(min = 5, max = 5)
    val wifiSignals: List<Int>,
    @field:NotNull
    val rsrp: Int,
    @field:NotNull
    val rssnr: Int,
    @field:NotNull
    val rsrq: Int,
    @field:NotNull
    @field:PositiveOrZero
    val latencyAvg: Double,
    @field:NotNull
    @field:PositiveOrZero
    val latencyStdDev: Double,
    @field:NotNull
    @field:Min(0)
    @field:Max(100)
    val packetLoss: Double,
)
