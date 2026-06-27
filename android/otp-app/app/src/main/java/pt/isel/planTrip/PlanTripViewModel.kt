package pt.isel.planTrip

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.isel.api.ApiAccess
import pt.isel.domain.metroStations
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PlanTripViewModel(private val api: ApiAccess) : ViewModel() {

    private val _selectedType = MutableStateFlow("")
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    private val _selectedStation = MutableStateFlow("")
    val selectedStation: StateFlow<String> = _selectedStation.asStateFlow()

    private val _selectedHour = MutableStateFlow("")
    val selectedHour: StateFlow<String> = _selectedHour.asStateFlow()

    private val _selectedDate = MutableStateFlow("")
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _occupancyResult = MutableStateFlow<String?>(null)
    val occupancyResult: StateFlow<String?> = _occupancyResult.asStateFlow()


    fun updateType(type: String) {
        _selectedType.value = type
        _selectedStation.value = ""
    }

    fun updateStation(station: String) {
        _selectedStation.value = station
    }

    fun updateHour(hour: String) {
        _selectedHour.value = hour
    }

    fun updateDate(date: String) {
        _selectedDate.value = date
    }

    fun checkOccupancyPrediction() {
        val type = _selectedType.value
        val station = _selectedStation.value
        val hour = _selectedHour.value
        val date = _selectedDate.value

        val stationId = metroStations.find { it.name == station }!!.stationId

        viewModelScope.launch {
            try {
                Log.d("API_DEBUG", "--- Antes ---")
                val time = getTimestampFromDate(date, hour)
                Log.d("API_DEBUG", "--- Depois ---")
                val occupancy = api.fetchStationOccupancy(stationId, time)

                Log.d("API_DEBUG", "Sucesso! Resposta bruta: $occupancy")
                _occupancyResult.value = "Lotação prevista: $occupancy"

            } catch (e: Exception) {
                Log.e("API_DEBUG", "Erro capturado: ${e.message}")
                Log.e("API_DEBUG", "Stack trace:", e)
                _occupancyResult.value = "Erro: ${e.message}"
            }
        }
        println("A pedir previsão para: $type na estação $station no dia $date às $hour")
    }

    private fun getTimestampFromDate(date: String, hour: String): Long {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        val combinedDateTime = LocalDateTime.parse("$date $hour", formatter)

        return combinedDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}