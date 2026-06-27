package pt.isel.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.isel.api.ApiAccess
import pt.isel.domain.Station

class MapViewModel(private val api: ApiAccess) : ViewModel() {

    private val _selectedStation = MutableStateFlow<Station?>(null)
    val selectedStation: StateFlow<Station?> = _selectedStation.asStateFlow()

    private val _occupancyResult = MutableStateFlow<String?>(null)
    val occupancyResult: StateFlow<String?> = _occupancyResult.asStateFlow()

    fun selectStation(station: Station) {
        _selectedStation.value = station
        _occupancyResult.value = "A contactar servidor..."

        viewModelScope.launch {
            Log.d("API_DEBUG", "--- A tentar ligar ao servidor ---")
            try {
                val occupancy = api.fetchStationOccupancy(station.stationId)

                Log.d("API_DEBUG", "Sucesso! Resposta bruta: $occupancy")
                _occupancyResult.value = "Lotação: $occupancy"

            } catch (e: Exception) {
                Log.e("API_DEBUG", "Erro capturado: ${e.message}")
                Log.e("API_DEBUG", "Stack trace:", e)
                _occupancyResult.value = "Erro: ${e.message}"
            }
        }
    }

    fun clearSelection() {
        _selectedStation.value = null
    }
}